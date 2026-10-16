package com.ptit.iot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record ActionHistoryDto(
    Long id,
    Integer deviceId,
    String deviceName,
    Integer sensorId,
    String sensorName,
    String action,
    String status,
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    LocalDateTime timestamp
) {
}
