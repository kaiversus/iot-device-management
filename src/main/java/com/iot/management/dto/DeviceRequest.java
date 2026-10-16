package com.iot.management.dto;

import com.iot.management.entity.DeviceStatus;
import javax.validation.constraints.*;

public class DeviceRequest {
    @NotBlank(message = "Vui lòng nhập tên thiết bị") @Size(max = 120)
    private String name;
    @NotBlank(message = "Vui lòng nhập mã thiết bị")
    @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9._-]{0,79}", message = "Mã gồm 1–80 chữ, số, dấu chấm, gạch ngang hoặc gạch dưới")
    private String serialNumber;
    @Size(max = 2000)
    private String description;
    @Size(max = 200)
    private String location;
    @NotNull(message = "Vui lòng chọn trạng thái")
    private DeviceStatus status = DeviceStatus.INACTIVE;
    @NotNull(message = "Vui lòng chọn danh mục") @Positive
    private Long categoryId;
    private boolean removeImage;

    public String getName() { return name; }
    public void setName(String name) { this.name = name == null ? null : name.trim(); }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber == null ? null : serialNumber.trim().toUpperCase(java.util.Locale.ROOT); }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public DeviceStatus getStatus() { return status; }
    public void setStatus(DeviceStatus status) { this.status = status; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public boolean isRemoveImage() { return removeImage; }
    public void setRemoveImage(boolean removeImage) { this.removeImage = removeImage; }
}
