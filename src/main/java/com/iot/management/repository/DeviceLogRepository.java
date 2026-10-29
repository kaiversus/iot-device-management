package com.iot.management.repository;

import com.iot.management.entity.DeviceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceLogRepository extends JpaRepository<DeviceLog, Long> {
    void deleteByDeviceId(Long deviceId);
    boolean existsByDeviceId(Long deviceId);

    List<DeviceLog> findTop10ByOrderByCreatedAtDesc();

    long countByLevel(String level);
List<DeviceLog> findTop10ByDeviceIdInOrderByCreatedAtDescIdDesc(java.util.Collection<Long> ids);
    long countByDeviceIdInAndLevel(java.util.Collection<Long> ids, String level);
}
