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
    public List<Category> list() { return categories.findAllByDeletedFalseOrderByNameAsc(); }
    public Category get(Long id) {
        Category category = categories.findById(id).orElseThrow(() -> new DeviceException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục"));
        if (category.isDeleted()) throw new DeviceException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục");
        return category;
    }
    @Transactional @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public Category save(Long id, CategoryRequest request) {
        Category category = id == null ? categories.findByNameIgnoreCase(request.getName())
            .filter(Category::isDeleted).orElseGet(Category::new) : get(id);
        boolean duplicate = id == null ? categories.existsByNameIgnoreCase(request.getName())
            : categories.existsByNameIgnoreCaseAndIdNot(request.getName(), id);
        if (duplicate && !category.isDeleted()) throw new DeviceException(HttpStatus.CONFLICT, "Tên danh mục đã tồn tại");
        category.setDeleted(false);
        category.setName(request.getName()); category.setDescription(request.getDescription());
        return categories.saveAndFlush(category);
    }
    @Transactional @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public void delete(Long id) {
        Category category = get(id);
        if (devices.existsByCategoryIdAndDeletedFalse(id))
            throw new DeviceException(HttpStatus.CONFLICT, "Danh mục còn thiết bị trong danh sách. Hãy xóa các thiết bị đó trước.");
        if (devices.existsByCategoryId(id)) {
            category.setDeleted(true);
            categories.saveAndFlush(category);
        } else {
            categories.delete(category);
            categories.flush();
        }
    }
}
