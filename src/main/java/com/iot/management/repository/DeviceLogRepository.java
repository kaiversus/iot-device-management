package com.iot.management.repository;

import com.iot.management.entity.DeviceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceLogRepository extends JpaRepository<DeviceLog, Long> {

    List<DeviceLog> findTop10ByOrderByCreatedAtDesc();

    long countByLevel(String level);
}
