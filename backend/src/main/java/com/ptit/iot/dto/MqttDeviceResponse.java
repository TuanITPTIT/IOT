package com.ptit.iot.dto;

public record MqttDeviceResponse(
    String deviceCode,
    String command,
    String status
) {
}
