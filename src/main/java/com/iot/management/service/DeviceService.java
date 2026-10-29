package com.iot.management.service;

import com.iot.management.dto.*;
import com.iot.management.entity.*;
import com.iot.management.exception.DeviceException;
import com.iot.management.repository.DeviceRepository;
import com.iot.management.security.CustomUserDetails;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
public class DeviceService {
    @org.springframework.beans.factory.annotation.Autowired private com.iot.management.repository.DeviceDataRepository readings;
    @org.springframework.beans.factory.annotation.Autowired private com.iot.management.repository.DeviceLogRepository logs;
    @org.springframework.beans.factory.annotation.Autowired private AuditService audit;
    private final DeviceRepository devices;
    private final CategoryService categories;
    private final CloudinaryService images;
    public DeviceService(DeviceRepository devices, CategoryService categories, CloudinaryService images) {
        this.devices = devices; this.categories = categories; this.images = images;
    }
    private boolean isAdmin(CustomUserDetails actor) {
        return actor.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
    private Device accessible(Long id, CustomUserDetails actor) {
        Device device = devices.findById(id).orElseThrow(this::notFound);
        if (device.isDeleted() || (!isAdmin(actor) && !device.getOwner().getId().equals(actor.getUser().getId()))) throw notFound();
        return device;
    }
    private DeviceException notFound() { return new DeviceException(HttpStatus.NOT_FOUND, "Không tìm thấy thiết bị"); }

    public Page<DeviceResponse> list(CustomUserDetails actor, String q, Long categoryId,
            DeviceStatus status, int page, int size) {
        if (page < 0 || size < 1 || size > 100 || (q != null && q.length() > 120))
            throw new DeviceException(HttpStatus.BAD_REQUEST, "Trang phải ≥ 0, kích thước 1–100, từ khóa tối đa 120 ký tự");
        Specification<Device> spec = (root, query, cb) -> cb.isFalse(root.get("deleted"));
        if (!isAdmin(actor)) {
            String region = actor.getUser().getRegion();
            boolean manager=actor.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_MANAGER"));
            spec = spec.and((root,query,cb) -> manager && region!=null && !region.isBlank()
                ? cb.or(cb.equal(root.get("owner").get("id"),actor.getUser().getId()),cb.equal(root.get("owner").get("region"),region))
                : cb.equal(root.get("owner").get("id"),actor.getUser().getId()));
        }
        if (categoryId != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        if (status != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        if (q != null && !q.isBlank()) {
            String term = "%" + q.trim().toLowerCase(java.util.Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
            spec = spec.and((root, query, cb) -> cb.or(cb.like(cb.lower(root.get("name")), term, '\\'),
                cb.like(cb.lower(root.get("serialNumber")), term, '\\')));
        }
        return devices.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id")))
            .map(DeviceResponse::from);
    }
    public DeviceResponse get(Long id, CustomUserDetails actor) {
        Device d=devices.findById(id).orElseThrow(this::notFound);
        boolean manager=actor.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_MANAGER"));
        if (d.isDeleted() || (!isAdmin(actor) && !d.getOwner().getId().equals(actor.getUser().getId())
            && !(manager && actor.getUser().getRegion()!=null && actor.getUser().getRegion().equals(d.getOwner().getRegion())))) throw notFound();
        return DeviceResponse.from(d);
    }

    @Transactional
    public DeviceResponse create(DeviceRequest request, MultipartFile image, CustomUserDetails actor) {
        Device device = new Device(); device.setOwner(actor.getUser());
        apply(device, request); replaceImage(device, image, false);
        return DeviceResponse.from(devices.saveAndFlush(device));
    }
    @Transactional
    public DeviceResponse update(Long id, DeviceRequest request, MultipartFile image, CustomUserDetails actor) {
        Device device = accessible(id, actor);
        apply(device, request); replaceImage(device, image, request.isRemoveImage());
        return DeviceResponse.from(devices.saveAndFlush(device));
    }
    @Transactional
    public DeviceResponse uploadImage(Long id, MultipartFile image, CustomUserDetails actor) {
        Device device = accessible(id, actor);
        if (image == null || image.isEmpty()) throw new DeviceException(HttpStatus.BAD_REQUEST, "Vui lòng chọn ảnh");
        replaceImage(device, image, false);
        return DeviceResponse.from(devices.saveAndFlush(device));
    }
    @Transactional
    public void delete(Long id, CustomUserDetails actor) {
        devices.lockForTelemetry(id).orElseThrow(this::notFound);
        Device device = accessible(id, actor);
        if (readings.existsByDeviceId(id) || logs.existsByDeviceId(id)) {
            if (device.getStatus() != DeviceStatus.INACTIVE)
                throw new DeviceException(HttpStatus.CONFLICT,"Thiết bị có lịch sử/cảnh báo. Hãy bấm Ngừng hoạt động trước khi xóa.");
            device.setDeleted(true);
            devices.saveAndFlush(device);
            audit.record("DEVICE_ARCHIVE", "device=" + id);
            return;
        }
        audit.record("DEVICE_DELETE","device="+id);
        cleanupAfterTransaction(null, device.getImagePublicId());
        devices.delete(device); devices.flush();
    }
    @Transactional
    public DeviceResponse deactivate(Long id, CustomUserDetails actor) {
        devices.lockForTelemetry(id).orElseThrow(this::notFound);
        Device device = accessible(id, actor);
        if (device.getStatus() != DeviceStatus.INACTIVE) {
            device.setStatus(DeviceStatus.INACTIVE);
            audit.record("DEVICE_DEACTIVATE", "device=" + id);
        }
        return DeviceResponse.from(devices.saveAndFlush(device));
    }
    private void apply(Device device, DeviceRequest request) {
        boolean duplicate = device.getId() == null ? devices.existsBySerialNumberIgnoreCase(request.getSerialNumber())
            : devices.existsBySerialNumberIgnoreCaseAndIdNot(request.getSerialNumber(), device.getId());
        if (duplicate) throw new DeviceException(HttpStatus.CONFLICT, "Mã thiết bị đã tồn tại");
        device.setCategory(categories.get(request.getCategoryId()));
        device.setName(request.getName()); device.setSerialNumber(request.getSerialNumber());
        device.setDescription(request.getDescription()); device.setLocation(request.getLocation()); device.setStatus(request.getStatus());
    }
    private void replaceImage(Device device, MultipartFile file, boolean remove) {
        if (file != null && !file.isEmpty()) {
            CloudinaryService.UploadedImage uploaded = images.upload(file);
            cleanupAfterTransaction(uploaded.publicId(), device.getImagePublicId());
            device.setImageUrl(uploaded.url()); device.setImagePublicId(uploaded.publicId());
        } else if (remove) {
            cleanupAfterTransaction(null, device.getImagePublicId());
            device.setImageUrl(null); device.setImagePublicId(null);
        }
    }
    private void cleanupAfterTransaction(String newImage, String oldImage) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) images.deleteQuietly(oldImage);
                else if (status == STATUS_ROLLED_BACK) images.deleteQuietly(newImage);
            }
        });
    }
}
