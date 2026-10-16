package com.iot.management.dto;

import com.iot.management.entity.Device;
import com.iot.management.entity.DeviceStatus;
import java.time.Instant;

public record DeviceResponse(Long id, String name, String serialNumber, String description,
        String location, DeviceStatus status, Long categoryId, String categoryName,
        Long ownerId, String ownerName, String imageUrl, Instant createdAt, Instant updatedAt) {
    public static DeviceResponse from(Device d) {
        return new DeviceResponse(d.getId(), d.getName(), d.getSerialNumber(), d.getDescription(),
            d.getLocation(), d.getStatus(), d.getCategory().getId(), d.getCategory().getName(),
            d.getOwner().getId(), d.getOwner().getFullName(), d.getImageUrl(), d.getCreatedAt(), d.getUpdatedAt());
    }
}
