package com.ptit.iot.service;

import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class DeviceCodeMapper {
    public String toDeviceCode(Integer deviceId) {
        return switch (deviceId) {
            case 1 -> "RED";
            case 2 -> "YELLOW";
            case 3 -> "GREEN";
            default -> throw new IllegalArgumentException("Unsupported deviceId: " + deviceId);
        };
    }

    public Integer toDeviceId(String deviceCode) {
        String normalized = normalize(deviceCode);
        return switch (normalized) {
            case "RED" -> 1;
            case "YELLOW" -> 2;
            case "GREEN" -> 3;
            default -> throw new IllegalArgumentException("Unsupported deviceCode: " + deviceCode);
        };
    }

    public boolean isAll(String deviceCode) {
        return "ALL".equals(normalize(deviceCode));
    }

    public String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Device code must not be blank");
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
