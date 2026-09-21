package com.iot.management.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Dữ liệu cảm biến mà thiết bị IoT gửi về (nhiệt độ, độ ẩm).
 *
 * LƯU Ý: hiện chưa có entity Device (phần của Đạt) nên tạm lưu deviceId dạng số.
 * Khi Đạt merge xong, đổi thành @ManyToOne Device device (xem docs/DANG_HUONG_DAN.md).
 */
@Entity
@Table(name = "device_data", indexes = @Index(name = "idx_device_time", columnList = "device_id, recorded_at"))
public class DeviceData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    private Double temperature;

    private Double humidity;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    void onCreate() {
        if (recordedAt == null) recordedAt = LocalDateTime.now();
    }

    public DeviceData() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getHumidity() { return humidity; }
    public void setHumidity(Double humidity) { this.humidity = humidity; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
