package com.ptit.iot.dto;

public record MqttSensorPayload(
    Float temperature,
    Float humidity,
    Float light
) {
}
