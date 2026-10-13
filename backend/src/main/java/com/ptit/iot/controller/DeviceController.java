package com.ptit.iot.controller;

import com.ptit.iot.dto.ApiResponse;
import com.ptit.iot.dto.DeviceControlRequest;
import com.ptit.iot.dto.DeviceControlResponse;
import com.ptit.iot.dto.DeviceDto;
import com.ptit.iot.service.DeviceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {
    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ApiResponse<List<DeviceDto>> devices() {
        return ApiResponse.success("Device list", deviceService.listDevices());
    }

    @PostMapping("/control")
    public ApiResponse<DeviceControlResponse> control(@Valid @RequestBody DeviceControlRequest request, Authentication authentication) {
        return ApiResponse.success("Device command sent", deviceService.control(request, authentication));
    }
}
