package com.iot.management.service;

import com.iot.management.dto.AlertMessage;
import com.iot.management.dto.DataPointMessage;
import com.iot.management.dto.IotDataRequest;
import com.iot.management.entity.DeviceData;
import com.iot.management.entity.DeviceLog;
import com.iot.management.repository.DeviceDataRepository;
import com.iot.management.repository.DeviceLogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class DeviceDataService {

    private final com.iot.management.repository.DeviceRepository devices;
    private final DeviceDataRepository dataRepository;
    private com.iot.management.security.CustomUserDetails actor() {
        return (com.iot.management.security.CustomUserDetails) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
    private boolean admin() { return actor().getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")); }
    private List<com.iot.management.entity.Device> visible() {
        if (admin()) return devices.findAll();
        return devices.findAll((root, query, cb) -> cb.equal(root.get("owner").get("id"), actor().getUser().getId()));
    }
    private List<Long> ids() { return visible().stream().map(com.iot.management.entity.Device::getId).collect(java.util.stream.Collectors.toList()); }
    private com.iot.management.entity.Device accessible(Long id) {
        com.iot.management.entity.Device d=devices.findById(id).orElseThrow(() -> new com.iot.management.exception.DeviceException(org.springframework.http.HttpStatus.NOT_FOUND,"Không tìm thấy thiết bị"));
        if (!admin() && !d.getOwner().getId().equals(actor().getUser().getId())) throw new com.iot.management.exception.DeviceException(org.springframework.http.HttpStatus.NOT_FOUND,"Không tìm thấy thiết bị");
        return d;
    }
    private void publish(String owner, String channel, Object payload) {
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(new org.springframework.transaction.support.TransactionSynchronization() {
            @Override public void afterCommit() {
                messagingTemplate.convertAndSendToUser(owner,"/queue/"+channel,payload);
                messagingTemplate.convertAndSend("/topic/"+channel,payload);
            }
        });
    }
    private final DeviceLogRepository logRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private final double temperatureThreshold;
    private final long alertCooldownSeconds;


    public DeviceDataService(com.iot.management.repository.DeviceRepository devices, DeviceDataRepository dataRepository,
                             DeviceLogRepository logRepository,
                             SimpMessagingTemplate messagingTemplate,
                             @Value("${iot.temperature-threshold:50}") double temperatureThreshold,
                             @Value("${iot.alert-cooldown-seconds:10}") long alertCooldownSeconds) {
        this.devices = devices;
        this.dataRepository = dataRepository;
        this.logRepository = logRepository;
        this.messagingTemplate = messagingTemplate;
        this.temperatureThreshold = temperatureThreshold;
        this.alertCooldownSeconds = alertCooldownSeconds;
    }

    /** Lưu dữ liệu -> đẩy điểm mới cho biểu đồ -> nếu vượt ngưỡng thì cảnh báo. */
    @Transactional
    public DeviceData save(IotDataRequest req) {
        devices.lockForTelemetry(req.getDeviceId()).orElseThrow(() -> new com.iot.management.exception.DeviceException(org.springframework.http.HttpStatus.NOT_FOUND, "Không tìm thấy thiết bị"));
        String owner = accessible(req.getDeviceId()).getOwner().getEmail();
        DeviceData data = new DeviceData();
        data.setDeviceId(req.getDeviceId());
        data.setTemperature(req.getTemperature());
        data.setHumidity(req.getHumidity());
        DeviceData saved = dataRepository.saveAndFlush(data);

        publish(owner, "data", new DataPointMessage(
                saved.getDeviceId(), saved.getTemperature(), saved.getHumidity(),
                saved.getRecordedAt().toString()));

        if (saved.getTemperature() > temperatureThreshold) {
            raiseAlert(saved, owner);
        }
        return saved;
    }

    private void raiseAlert(DeviceData d, String owner) {
        Instant now = Instant.now();
        List<DeviceLog> recent = logRepository.findTop10ByDeviceIdInOrderByCreatedAtDescIdDesc(List.of(d.getDeviceId()));
        if (!recent.isEmpty() && Duration.between(recent.get(0).getCreatedAt(), Instant.now()).getSeconds() < alertCooldownSeconds) return;

        String text = "Cảnh báo: Nhiệt độ thiết bị " + d.getDeviceId()
                + " đang quá cao (" + d.getTemperature() + "°C)!";
        logRepository.saveAndFlush(new DeviceLog(d.getDeviceId(), "WARNING", text));
        publish(owner, "alerts", new AlertMessage(
                d.getDeviceId(), d.getTemperature(), text, now.toString()));
    }

    /** Lịch sử gần nhất (cũ -> mới) để vẽ biểu đồ khi mở trang. */
    @Transactional(readOnly = true)
    public List<DeviceData> history(Long deviceId) {
        List<Long> scope = deviceId == null ? ids() : List.of(accessible(deviceId).getId());
        if (scope.isEmpty()) return List.of();
        List<DeviceData> list = dataRepository.findTop20ByDeviceIdInOrderByRecordedAtDescIdDesc(scope);
        Collections.reverse(list);
        return list;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> stats() {
        Map<String, Object> m = new HashMap<>();
        List<com.iot.management.entity.Device> scope = visible();
        List<Long> ids = scope.stream().map(com.iot.management.entity.Device::getId).collect(java.util.stream.Collectors.toList());
        m.put("totalDevices", scope.size());
        for (com.iot.management.entity.DeviceStatus status : com.iot.management.entity.DeviceStatus.values()) {
            m.put(status.name().toLowerCase()+"Devices", scope.stream().filter(d -> d.getStatus()==status).count());
        }
        m.put("totalRecords", ids.isEmpty() ? 0 : dataRepository.countByDeviceIdIn(ids));
        m.put("reportingDevices", ids.isEmpty() ? 0 : dataRepository.countReporting(ids));
        m.put("totalAlerts", ids.isEmpty() ? 0 : logRepository.countByDeviceIdInAndLevel(ids,"WARNING"));
        m.put("threshold", temperatureThreshold);
        return m;
    }

    @Transactional(readOnly = true)
    public List<DeviceLog> recentLogs() {
        List<Long> scope=ids();
        return scope.isEmpty() ? List.of() : logRepository.findTop10ByDeviceIdInOrderByCreatedAtDescIdDesc(scope);
    }
}
