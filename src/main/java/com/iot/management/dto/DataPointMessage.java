package com.iot.management.dto;

/** Một điểm dữ liệu đẩy realtime xuống biểu đồ (/topic/data). */
public class DataPointMessage {
    private final Long deviceId;
    private final Double temperature;
    private final Double humidity;
    private final String time; // ISO-8601

    public DataPointMessage(Long deviceId, Double temperature, Double humidity, String time) {
        this.deviceId = deviceId;
        this.temperature = temperature;
        this.humidity = humidity;
        this.time = time;
    }

    public Long getDeviceId() { return deviceId; }
    public Double getTemperature() { return temperature; }
    public Double getHumidity() { return humidity; }
    public String getTime() { return time; }
}
