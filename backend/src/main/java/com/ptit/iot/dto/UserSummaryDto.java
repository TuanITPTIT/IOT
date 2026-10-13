package com.ptit.iot.dto;

public record UserSummaryDto(
    Long id,
    String username,
    String fullName,
    String role
) {
}
