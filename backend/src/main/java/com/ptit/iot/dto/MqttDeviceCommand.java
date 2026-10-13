package com.ptit.iot.dto;

public record MqttDeviceCommand(
    String deviceCode,
    String command
) {
}
