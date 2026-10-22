package com.iot.management.entity;

import javax.persistence.*;
import java.time.Instant;

/** Dữ liệu IoT gắn với ID thiết bị đã kiểm tra quyền sở hữu. */
@Entity
@Table(name = "device_data", indexes = @Index(name = "idx_device_time", columnList = "device_id, recorded_at"))
public class DeviceData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="device_id", insertable=false, updatable=false, foreignKey=@ForeignKey(name="fk_devicedata_device"))
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Device device;

    private Double temperature;

    private Double humidity;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @PrePersist
    void onCreate() {
        if (recordedAt == null) recordedAt = Instant.now();
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

    public Instant getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }
}
