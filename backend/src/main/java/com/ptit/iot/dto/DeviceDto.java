package com.ptit.iot.dto;

public record DeviceDto(
    Integer id,
    String code,
    String name,
    String status
) {
}
