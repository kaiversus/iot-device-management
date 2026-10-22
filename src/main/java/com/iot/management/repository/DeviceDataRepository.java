package com.iot.management.repository;

import com.iot.management.entity.DeviceData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceDataRepository extends JpaRepository<DeviceData, Long> {
    boolean existsByDeviceId(Long deviceId);

    List<DeviceData> findTop20ByDeviceIdOrderByRecordedAtDesc(Long deviceId);

    List<DeviceData> findTop20ByOrderByRecordedAtDesc();

    @Query("select count(distinct d.deviceId) from DeviceData d")
    long countDistinctDevices();
List<DeviceData> findTop20ByDeviceIdInOrderByRecordedAtDescIdDesc(java.util.Collection<Long> ids);
    long countByDeviceIdIn(java.util.Collection<Long> ids);
    @Query("select count(distinct d.deviceId) from DeviceData d where d.deviceId in :ids")
    long countReporting(@org.springframework.data.repository.query.Param("ids") java.util.Collection<Long> ids);
}
