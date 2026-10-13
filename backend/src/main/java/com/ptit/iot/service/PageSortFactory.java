package com.ptit.iot.service;

import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PageSortFactory {
    public Pageable create(
        int page,
        int size,
        String sort,
        String sortBy,
        String sortDir,
        String defaultSort,
        Set<String> allowedFields
    ) {
        String field = defaultSort;
        String direction = "desc";

        if (StringUtils.hasText(sort)) {
            String[] parts = sort.split(",");
            field = parts[0].trim();
            if (parts.length > 1) {
                direction = parts[1].trim();
            }
        } else {
            if (StringUtils.hasText(sortBy)) {
                field = sortBy.trim();
            }
            if (StringUtils.hasText(sortDir)) {
                direction = sortDir.trim();
            }
        }

        field = normalizeField(field);
        if (!allowedFields.contains(field)) {
            field = defaultSort;
        }
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(sortDirection, field));
    }

    private String normalizeField(String field) {
        if ("timestamp".equalsIgnoreCase(field)) {
            return "time";
        }
        if ("created_at".equalsIgnoreCase(field)) {
            return "createdAt";
        }
        return field;
    }
}
