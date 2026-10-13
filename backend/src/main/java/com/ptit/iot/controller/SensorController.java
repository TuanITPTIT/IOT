package com.ptit.iot.controller;

import com.ptit.iot.dto.ApiResponse;
import com.ptit.iot.dto.ChartDataPoint;
import com.ptit.iot.dto.DataSensorDto;
import com.ptit.iot.dto.LatestSensorResponse;
import com.ptit.iot.dto.PageResponse;
import com.ptit.iot.service.SensorService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sensors")
public class SensorController {
    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @GetMapping("/latest")
    public ApiResponse<LatestSensorResponse> latest() {
        return ApiResponse.success("Latest sensor data", sensorService.latest());
    }

    @GetMapping("/chart-data")
    public ApiResponse<List<ChartDataPoint>> chartData(@RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.success("Sensor chart data", sensorService.chartData(limit));
    }

    @GetMapping("/history")
    public ApiResponse<PageResponse<DataSensorDto>> history(
        @RequestParam(required = false, defaultValue = "all") String searchBy,
        @RequestParam(required = false) String query,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String sensorType,
        @RequestParam(required = false) String value,
        @RequestParam(required = false) String fromTime,
        @RequestParam(required = false) String toTime,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false) String sortDir
    ) {
        String effectiveQuery = query != null ? query : keyword;
        return ApiResponse.success(
            "Sensor history",
            sensorService.history(searchBy, effectiveQuery, sensorType, value, fromTime, toTime, page, size, sort, sortBy, sortDir)
        );
    }
}
