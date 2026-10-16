package com.ptit.iot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record DataSensorDto(
    Long id,
    Integer sensorId,
    String sensorType,
    Integer deviceId,
    String deviceName,
    Float value,
    String unit,
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    LocalDateTime timestamp
) {
}
