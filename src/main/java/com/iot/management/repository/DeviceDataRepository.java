package com.iot.management.repository;

import com.iot.management.entity.DeviceData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceDataRepository extends JpaRepository<DeviceData, Long> {

    List<DeviceData> findTop20ByDeviceIdOrderByRecordedAtDesc(Long deviceId);

    List<DeviceData> findTop20ByOrderByRecordedAtDesc();

    @Query("select count(distinct d.deviceId) from DeviceData d")
    long countDistinctDevices();
}
