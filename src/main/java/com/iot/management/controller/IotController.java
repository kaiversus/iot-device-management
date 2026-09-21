package com.iot.management.controller;

import com.iot.management.dto.IotDataRequest;
import com.iot.management.entity.DeviceData;
import com.iot.management.entity.DeviceLog;
import com.iot.management.service.DeviceDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/iot")
public class IotController {

    private final DeviceDataService service;

    public IotController(DeviceDataService service) {
        this.service = service;
    }

    /** Postman / thiết bị giả lập gửi dữ liệu về (công khai, xem SecurityConfig). */
    @PostMapping("/data")
    public ResponseEntity<Map<String, Object>> receive(@Valid @RequestBody IotDataRequest req) {
        DeviceData saved = service.save(req);
        Map<String, Object> body = new HashMap<>();
        body.put("status", "ok");
        body.put("id", saved.getId());
        return ResponseEntity.ok(body);
    }

    /** 20 điểm gần nhất. Bỏ deviceId để lấy của mọi thiết bị. */
    @GetMapping("/data")
    public List<DeviceData> history(@RequestParam(required = false) Long deviceId) {
        return service.history(deviceId);
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return service.stats();
    }

    @GetMapping("/logs")
    public List<DeviceLog> logs() {
        return service.recentLogs();
    }
}
