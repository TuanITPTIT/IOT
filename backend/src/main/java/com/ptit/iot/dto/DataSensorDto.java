package com.ptit.iot.dto;

import java.time.LocalDateTime;

public record DataSensorDto(
    Long id,
    Integer sensorId,
    String sensorType,
    Integer deviceId,
    String deviceName,
    Float value,
    String unit,
    LocalDateTime timestamp
) {
}
