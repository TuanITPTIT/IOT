package com.ptit.iot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record DeviceControlResponse(
    Long actionId,
    Integer deviceId,
    String deviceCode,
    String deviceName,
    String action,
    String status,
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    LocalDateTime timestamp
) {
}
