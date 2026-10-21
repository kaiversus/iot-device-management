package com.iot.management.service;

import com.iot.management.dto.CategoryRequest;
import com.iot.management.entity.Category;
import com.iot.management.exception.DeviceException;
import com.iot.management.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
public class CategoryService {
    private final CategoryRepository categories;
    private final DeviceRepository devices;
    public CategoryService(CategoryRepository categories, DeviceRepository devices) {
        this.categories = categories; this.devices = devices;
    }
    public List<Category> list() { return categories.findAllByOrderByNameAsc(); }
    public Category get(Long id) {
        return categories.findById(id).orElseThrow(() -> new DeviceException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục"));
    }
    @Transactional @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public Category save(Long id, CategoryRequest request) {
        Category category = id == null ? new Category() : get(id);
        boolean duplicate = id == null ? categories.existsByNameIgnoreCase(request.getName())
            : categories.existsByNameIgnoreCaseAndIdNot(request.getName(), id);
        if (duplicate) throw new DeviceException(HttpStatus.CONFLICT, "Tên danh mục đã tồn tại");
        category.setName(request.getName()); category.setDescription(request.getDescription());
        return categories.saveAndFlush(category);
    }
    @Transactional @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public void delete(Long id) {
        Category category = get(id);
        if (devices.existsByCategoryId(id)) throw new DeviceException(HttpStatus.CONFLICT, "Danh mục đang được thiết bị sử dụng, không thể xóa");
        categories.delete(category); categories.flush();
    }
}
