package com.iot.management.dto;

/** Cảnh báo vượt ngưỡng đẩy realtime xuống client (/topic/alerts). */
public class AlertMessage {
    private final Long deviceId;
    private final Double temperature;
    private final String message;
    private final String time; // ISO-8601

    public AlertMessage(Long deviceId, Double temperature, String message, String time) {
        this.deviceId = deviceId;
        this.temperature = temperature;
        this.message = message;
        this.time = time;
    }

    public Long getDeviceId() { return deviceId; }
    public Double getTemperature() { return temperature; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
}
