package com.ptit.iot.dto;

import java.time.LocalDateTime;

public record LatestSensorResponse(
    Float temperature,
    Float luminosity,
    Float humidity,
    LocalDateTime timestamp
) {
}
