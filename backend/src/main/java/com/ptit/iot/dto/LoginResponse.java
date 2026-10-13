package com.ptit.iot.dto;

public record LoginResponse(
    String token,
    String tokenType,
    UserSummaryDto user
) {
}
