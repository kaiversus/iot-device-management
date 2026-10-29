package com.iot.management.repository;

import com.iot.management.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DeviceRepository extends JpaRepository<Device, Long>, JpaSpecificationExecutor<Device> {
    java.util.List<Device> findByOwnerId(Long ownerId);
    @org.springframework.data.jpa.repository.Lock(javax.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select d from Device d where d.id = :id")
    java.util.Optional<Device> lockForTelemetry(@org.springframework.data.repository.query.Param("id") Long id);
    boolean existsBySerialNumberIgnoreCase(String serialNumber);
    boolean existsBySerialNumberIgnoreCaseAndIdNot(String serialNumber, Long id);
    boolean existsByCategoryId(Long categoryId);
    boolean existsByCategoryIdAndDeletedFalse(Long categoryId);
}
