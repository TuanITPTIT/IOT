package com.ptit.iot.dto;

import jakarta.validation.constraints.NotBlank;

public record DeviceControlRequest(
    Integer deviceId,
    String deviceCode,
    @NotBlank String action
) {
}
