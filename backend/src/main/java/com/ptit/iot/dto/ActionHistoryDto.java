package com.ptit.iot.dto;

import java.time.LocalDateTime;

public record ActionHistoryDto(
    Long id,
    Integer deviceId,
    String deviceName,
    Integer sensorId,
    String sensorName,
    String action,
    String status,
    LocalDateTime timestamp
) {
}
