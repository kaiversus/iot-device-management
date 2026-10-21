package com.iot.management.exception;

import org.springframework.http.HttpStatus;

public class DeviceException extends RuntimeException {
    private final HttpStatus status;
    public DeviceException(HttpStatus status, String message) { super(message); this.status = status; }
    public HttpStatus getStatus() { return status; }
}
