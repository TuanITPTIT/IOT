package com.ptit.iot.dto;

public record ChartDataPoint(
    String time,
    Float temperature,
    Float humidity,
    Float luminosity
) {
}
