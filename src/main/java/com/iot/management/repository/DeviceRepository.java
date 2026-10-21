package com.iot.management.repository;

import com.iot.management.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DeviceRepository extends JpaRepository<Device, Long>, JpaSpecificationExecutor<Device> {
    boolean existsBySerialNumberIgnoreCase(String serialNumber);
    boolean existsBySerialNumberIgnoreCaseAndIdNot(String serialNumber, Long id);
    boolean existsByCategoryId(Long categoryId);
}
