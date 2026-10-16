package com.ptit.iot.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TimeParser {
    private static final List<DateTimeFormatter> DATE_TIME_FORMATS = List.of(
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ISO_LOCAL_DATE_TIME
    );

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ISO_LOCAL_DATE
    );

    public LocalDateTime parseStart(String value) {
        return parse(value, true);
    }

    public LocalDateTime parseEnd(String value) {
        return parse(value, false);
    }

    private LocalDateTime parse(String value, boolean startOfDay) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String text = value.trim();
        for (DateTimeFormatter formatter : DATE_TIME_FORMATS) {
            try {
                return LocalDateTime.parse(text, formatter).withNano(0);
            } catch (DateTimeParseException ignored) {
            }
        }
        for (DateTimeFormatter formatter : DATE_FORMATS) {
            try {
                LocalDate date = LocalDate.parse(text, formatter);
                return LocalDateTime.of(date, startOfDay ? LocalTime.MIN : LocalTime.MAX.withNano(0));
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new IllegalArgumentException("Invalid time format. Use yyyy/MM/dd HH:mm:ss");
    }
}
