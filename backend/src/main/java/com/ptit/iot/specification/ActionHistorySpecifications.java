package com.ptit.iot.specification;

import com.ptit.iot.entity.ActionHistory;
import jakarta.persistence.criteria.JoinType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ActionHistorySpecifications {
    private static final Pattern VN_DATE_FIRST = Pattern.compile("^(\\d{2})[/-](\\d{2})[/-](\\d{4})(?:\\s+(\\d{1,2}:\\d{2}(?::\\d{2})?))?$");
    private static final Pattern ISO_DATE_FIRST = Pattern.compile("^(\\d{4})[/-](\\d{2})[/-](\\d{2})(?:\\s+(\\d{1,2}:\\d{2}(?::\\d{2})?))?$");
    private static final Pattern TIME_FIRST_VN_DATE = Pattern.compile("^(\\d{1,2}:\\d{2}(?::\\d{2})?)\\s+(\\d{2})[/-](\\d{2})[/-](\\d{4})$");
    private static final Pattern TIME_FIRST_ISO_DATE = Pattern.compile("^(\\d{1,2}:\\d{2}(?::\\d{2})?)\\s+(\\d{4})[/-](\\d{2})[/-](\\d{2})$");

    private ActionHistorySpecifications() {
    }

    public static Specification<ActionHistory> filter(
        Integer deviceId,
        String deviceName,
        String action,
        String status,
        String time,
        LocalDateTime fromTime,
        LocalDateTime toTime
    ) {
        return (root, criteriaQuery, cb) -> {
            root.join("device", JoinType.LEFT);
            root.join("sensor", JoinType.LEFT);
            criteriaQuery.distinct(true);
            var predicate = cb.conjunction();

            if (deviceId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("device").get("id"), deviceId));
            }
            if (StringUtils.hasText(deviceName) && !"All".equalsIgnoreCase(deviceName)) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("device").get("name")), "%" + deviceName.toLowerCase() + "%"));
            }
            if (StringUtils.hasText(action) && !"All".equalsIgnoreCase(action)) {
                predicate = cb.and(predicate, cb.equal(cb.upper(root.get("action")), action.toUpperCase()));
            }
            if (StringUtils.hasText(status) && !"All".equalsIgnoreCase(status)) {
                predicate = cb.and(predicate, cb.equal(cb.lower(root.get("status")), status.toLowerCase()));
            }
            if (StringUtils.hasText(time)) {
                var timePredicate = cb.disjunction();
                TimeRange range = parseTimeRange(time);
                if (range != null) {
                    timePredicate = cb.or(
                        timePredicate,
                        cb.and(
                        cb.greaterThanOrEqualTo(root.get("createdAt"), range.start()),
                        cb.lessThan(root.get("createdAt"), range.end())
                        )
                    );
                }
                for (String term : timeSearchTerms(time)) {
                    timePredicate = cb.or(timePredicate, cb.like(root.get("createdAt").as(String.class), "%" + term + "%"));
                }
                predicate = cb.and(predicate, timePredicate);
            }
            if (fromTime != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("createdAt"), fromTime));
            }
            if (toTime != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("createdAt"), toTime));
            }
            return predicate;
        };
    }

    private static List<String> timeSearchTerms(String value) {
        String text = value.trim().replaceAll("\\s+", " ");
        List<String> terms = new ArrayList<>();
        terms.add(text);
        terms.add(text.replace('-', '/'));
        terms.add(text.replace('/', '-'));

        Matcher vnDateFirst = VN_DATE_FIRST.matcher(text);
        if (vnDateFirst.matches()) {
            addDateTimeTerms(terms, vnDateFirst.group(1), vnDateFirst.group(2), vnDateFirst.group(3), vnDateFirst.group(4));
        }

        Matcher isoDateFirst = ISO_DATE_FIRST.matcher(text);
        if (isoDateFirst.matches()) {
            addDateTimeTerms(terms, isoDateFirst.group(3), isoDateFirst.group(2), isoDateFirst.group(1), isoDateFirst.group(4));
        }

        Matcher timeFirstVnDate = TIME_FIRST_VN_DATE.matcher(text);
        if (timeFirstVnDate.matches()) {
            addDateTimeTerms(terms, timeFirstVnDate.group(2), timeFirstVnDate.group(3), timeFirstVnDate.group(4), timeFirstVnDate.group(1));
        }

        Matcher timeFirstIsoDate = TIME_FIRST_ISO_DATE.matcher(text);
        if (timeFirstIsoDate.matches()) {
            addDateTimeTerms(terms, timeFirstIsoDate.group(4), timeFirstIsoDate.group(3), timeFirstIsoDate.group(2), timeFirstIsoDate.group(1));
        }

        return terms.stream().distinct().toList();
    }

    private static TimeRange parseTimeRange(String value) {
        String text = value.trim().replaceAll("\\s+", " ");

        Matcher vnDateFirst = VN_DATE_FIRST.matcher(text);
        if (vnDateFirst.matches()) {
            return toRange(vnDateFirst.group(1), vnDateFirst.group(2), vnDateFirst.group(3), vnDateFirst.group(4));
        }

        Matcher isoDateFirst = ISO_DATE_FIRST.matcher(text);
        if (isoDateFirst.matches()) {
            return toRange(isoDateFirst.group(3), isoDateFirst.group(2), isoDateFirst.group(1), isoDateFirst.group(4));
        }

        Matcher timeFirstVnDate = TIME_FIRST_VN_DATE.matcher(text);
        if (timeFirstVnDate.matches()) {
            return toRange(timeFirstVnDate.group(2), timeFirstVnDate.group(3), timeFirstVnDate.group(4), timeFirstVnDate.group(1));
        }

        Matcher timeFirstIsoDate = TIME_FIRST_ISO_DATE.matcher(text);
        if (timeFirstIsoDate.matches()) {
            return toRange(timeFirstIsoDate.group(4), timeFirstIsoDate.group(3), timeFirstIsoDate.group(2), timeFirstIsoDate.group(1));
        }

        return null;
    }

    private static TimeRange toRange(String day, String month, String year, String time) {
        LocalDate date = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
        if (!StringUtils.hasText(time)) {
            return new TimeRange(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
        }

        LocalTime localTime = LocalTime.parse(normalizeTime(time));
        LocalDateTime start = LocalDateTime.of(date, localTime);
        return new TimeRange(start, start.plusSeconds(1));
    }

    private static void addDateTimeTerms(List<String> terms, String day, String month, String year, String time) {
        String vnDate = day + "/" + month + "/" + year;
        String isoDate = year + "-" + month + "-" + day;
        if (StringUtils.hasText(time)) {
            String normalizedTime = normalizeTime(time);
            terms.add(isoDate + " " + normalizedTime);
            terms.add(isoDate + "T" + normalizedTime);
            terms.add(vnDate + " " + normalizedTime);
            terms.add(normalizedTime + " " + vnDate);
            return;
        }

        terms.add(vnDate);
        terms.add(day + "-" + month + "-" + year);
        terms.add(isoDate);
        terms.add(year + "/" + month + "/" + day);
    }

    private static String normalizeTime(String time) {
        String[] parts = time.trim().split(":");
        if (parts.length == 0 || parts[0].length() != 1) {
            return time.trim();
        }
        return "0" + time.trim();
    }

    private record TimeRange(LocalDateTime start, LocalDateTime end) {
    }
}
