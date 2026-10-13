package com.ptit.iot.dto;

import java.time.LocalDateTime;

public record DeviceControlResponse(
    Long actionId,
    Integer deviceId,
    String deviceCode,
    String deviceName,
    String action,
    String status,
    LocalDateTime timestamp
) {
}
