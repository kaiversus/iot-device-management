package com.iot.management.entity;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "devices", indexes = {
    @Index(name = "idx_device_owner", columnList = "owner_id"),
    @Index(name = "idx_device_category", columnList = "category_id")
})
public class Device {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version
    private Long version;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, unique = true, length = 80)
    private String serialNumber;
    @Column(length = 2000)
    private String description;
    @Column(length = 200)
    private String location;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private DeviceStatus status = DeviceStatus.INACTIVE;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    @Column(length = 1000)
    private String imageUrl;
    private String imagePublicId;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() { createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVersion() { return version; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public DeviceStatus getStatus() { return status; }
    public void setStatus(DeviceStatus status) { this.status = status; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getImagePublicId() { return imagePublicId; }
    public void setImagePublicId(String imagePublicId) { this.imagePublicId = imagePublicId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
