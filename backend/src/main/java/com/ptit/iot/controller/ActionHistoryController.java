package com.ptit.iot.controller;

import com.ptit.iot.dto.ActionHistoryDto;
import com.ptit.iot.dto.ApiResponse;
import com.ptit.iot.dto.PageResponse;
import com.ptit.iot.service.ActionHistoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/actions")
public class ActionHistoryController {
    private final ActionHistoryService actionHistoryService;

    public ActionHistoryController(ActionHistoryService actionHistoryService) {
        this.actionHistoryService = actionHistoryService;
    }

    @GetMapping("/history")
    public ApiResponse<PageResponse<ActionHistoryDto>> history(
        @RequestParam(required = false) Integer deviceId,
        @RequestParam(required = false) String deviceName,
        @RequestParam(required = false) String action,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String time,
        @RequestParam(required = false) String fromTime,
        @RequestParam(required = false) String toTime,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false) String sortDir
    ) {
        return ApiResponse.success(
            "Action history",
            actionHistoryService.history(deviceId, deviceName, action, status, time, fromTime, toTime, page, size, sort, sortBy, sortDir)
        );
    }
}
