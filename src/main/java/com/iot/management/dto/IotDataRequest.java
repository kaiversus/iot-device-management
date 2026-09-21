package com.iot.management.dto;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

/** Body JSON mà Postman / thiết bị IoT gửi lên POST /api/iot/data. */
public class IotDataRequest {

    @NotNull(message = "deviceId không được để trống")
    private Long deviceId;

    @NotNull(message = "temperature không được để trống")
    @DecimalMin(value = "-50", message = "Nhiệt độ phải >= -50")
    @DecimalMax(value = "150", message = "Nhiệt độ phải <= 150")
    private Double temperature;

    @NotNull(message = "humidity không được để trống")
    @DecimalMin(value = "0", message = "Độ ẩm phải >= 0")
    @DecimalMax(value = "100", message = "Độ ẩm phải <= 100")
    private Double humidity;

    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getHumidity() { return humidity; }
    public void setHumidity(Double humidity) { this.humidity = humidity; }
}
