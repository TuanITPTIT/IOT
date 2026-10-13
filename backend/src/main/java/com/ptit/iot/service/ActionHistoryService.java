package com.ptit.iot.service;

import com.ptit.iot.dto.ActionHistoryDto;
import com.ptit.iot.dto.PageResponse;
import com.ptit.iot.entity.ActionHistory;
import com.ptit.iot.repository.ActionHistoryRepository;
import com.ptit.iot.specification.ActionHistorySpecifications;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActionHistoryService {
    private final ActionHistoryRepository actionHistoryRepository;
    private final DtoMapper dtoMapper;
    private final TimeParser timeParser;
    private final PageSortFactory pageSortFactory;

    public ActionHistoryService(
        ActionHistoryRepository actionHistoryRepository,
        DtoMapper dtoMapper,
        TimeParser timeParser,
        PageSortFactory pageSortFactory
    ) {
        this.actionHistoryRepository = actionHistoryRepository;
        this.dtoMapper = dtoMapper;
        this.timeParser = timeParser;
        this.pageSortFactory = pageSortFactory;
    }

    @Transactional(readOnly = true)
    public PageResponse<ActionHistoryDto> history(
        Integer deviceId,
        String deviceName,
        String action,
        String status,
        String time,
        String fromTime,
        String toTime,
        int page,
        int size,
        String sort,
        String sortBy,
        String sortDir
    ) {
        Pageable pageable = pageSortFactory.create(page, size, sort, sortBy, sortDir, "createdAt", Set.of("id", "action", "status", "createdAt"));
        Page<ActionHistory> result = actionHistoryRepository.findAll(
            ActionHistorySpecifications.filter(deviceId, deviceName, action, status, time, timeParser.parseStart(fromTime), timeParser.parseEnd(toTime)),
            pageable
        );
        return new PageResponse<>(
            result.getContent().stream().map(dtoMapper::toActionHistoryDto).toList(),
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
    }
}
