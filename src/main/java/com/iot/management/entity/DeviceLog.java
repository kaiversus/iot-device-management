package com.iot.management.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/** Nhật ký sự kiện của thiết bị (hiện dùng để lưu các cảnh báo vượt ngưỡng). */
@Entity
@Table(name = "device_logs")
public class DeviceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(length = 20, nullable = false)
    private String level; // INFO | WARNING | ERROR

    @Column(length = 255, nullable = false)
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public DeviceLog() {}

    public DeviceLog(Long deviceId, String level, String message) {
        this.deviceId = deviceId;
        this.level = level;
        this.message = message;
    }

    public Long getId() { return id; }
    public Long getDeviceId() { return deviceId; }
    public String getLevel() { return level; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
