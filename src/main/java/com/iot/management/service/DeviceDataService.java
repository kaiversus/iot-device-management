package com.iot.management.service;

import com.iot.management.dto.AlertMessage;
import com.iot.management.dto.DataPointMessage;
import com.iot.management.dto.IotDataRequest;
import com.iot.management.entity.DeviceData;
import com.iot.management.entity.DeviceLog;
import com.iot.management.repository.DeviceDataRepository;
import com.iot.management.repository.DeviceLogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DeviceDataService {

    private final DeviceDataRepository dataRepository;
    private final DeviceLogRepository logRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private final double temperatureThreshold;
    private final long alertCooldownSeconds;

    /** deviceId -> thời điểm cảnh báo gần nhất (chống spam Toast khi demo). */
    private final Map<Long, Instant> lastAlertAt = new ConcurrentHashMap<>();

    public DeviceDataService(DeviceDataRepository dataRepository,
                             DeviceLogRepository logRepository,
                             SimpMessagingTemplate messagingTemplate,
                             @Value("${iot.temperature-threshold:50}") double temperatureThreshold,
                             @Value("${iot.alert-cooldown-seconds:10}") long alertCooldownSeconds) {
        this.dataRepository = dataRepository;
        this.logRepository = logRepository;
        this.messagingTemplate = messagingTemplate;
        this.temperatureThreshold = temperatureThreshold;
        this.alertCooldownSeconds = alertCooldownSeconds;
    }

    /** Lưu dữ liệu -> đẩy điểm mới cho biểu đồ -> nếu vượt ngưỡng thì cảnh báo. */
    @Transactional
    public DeviceData save(IotDataRequest req) {
        // TODO(khi có entity Device của Đạt): kiểm tra deviceRepository.existsById(req.getDeviceId())
        DeviceData data = new DeviceData();
        data.setDeviceId(req.getDeviceId());
        data.setTemperature(req.getTemperature());
        data.setHumidity(req.getHumidity());
        DeviceData saved = dataRepository.save(data);

        messagingTemplate.convertAndSend("/topic/data", new DataPointMessage(
                saved.getDeviceId(), saved.getTemperature(), saved.getHumidity(),
                saved.getRecordedAt().toString()));

        if (saved.getTemperature() > temperatureThreshold) {
            raiseAlert(saved);
        }
        return saved;
    }

    private void raiseAlert(DeviceData d) {
        Instant now = Instant.now();
        Instant last = lastAlertAt.get(d.getDeviceId());
        if (last != null && Duration.between(last, now).getSeconds() < alertCooldownSeconds) {
            return; // vẫn trong thời gian chờ
        }
        lastAlertAt.put(d.getDeviceId(), now);

        String text = "Cảnh báo: Nhiệt độ thiết bị " + d.getDeviceId()
                + " đang quá cao (" + d.getTemperature() + "°C)!";
        logRepository.save(new DeviceLog(d.getDeviceId(), "WARNING", text));
        messagingTemplate.convertAndSend("/topic/alerts", new AlertMessage(
                d.getDeviceId(), d.getTemperature(), text, now.toString()));
    }

    /** Lịch sử gần nhất (cũ -> mới) để vẽ biểu đồ khi mở trang. */
    @Transactional(readOnly = true)
    public List<DeviceData> history(Long deviceId) {
        List<DeviceData> list = deviceId == null
                ? dataRepository.findTop20ByOrderByRecordedAtDesc()
                : dataRepository.findTop20ByDeviceIdOrderByRecordedAtDesc(deviceId);
        Collections.reverse(list);
        return list;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> stats() {
        Map<String, Object> m = new HashMap<>();
        m.put("totalRecords", dataRepository.count());
        m.put("reportingDevices", dataRepository.countDistinctDevices());
        m.put("totalAlerts", logRepository.countByLevel("WARNING"));
        m.put("threshold", temperatureThreshold);
        return m;
    }

    @Transactional(readOnly = true)
    public List<DeviceLog> recentLogs() {
        return logRepository.findTop10ByOrderByCreatedAtDesc();
    }
}
