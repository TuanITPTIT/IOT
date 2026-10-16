package com.ptit.iot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record LatestSensorResponse(
    Float temperature,
    Float luminosity,
    Float humidity,
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    LocalDateTime timestamp
) {
}
