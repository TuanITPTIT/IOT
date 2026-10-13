package com.ptit.iot.dto;

import java.util.Map;

public record ProfileResponse(
    Long id,
    String username,
    String fullName,
    String role,
    String studentId,
    String className,
    String institution,
    String aboutMe,
    Map<String, String> contact,
    Map<String, String> resources
) {
}
