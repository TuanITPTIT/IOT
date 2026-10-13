package com.ptit.iot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptit.iot.dto.DeviceControlRequest;
import com.ptit.iot.dto.DeviceControlResponse;
import com.ptit.iot.dto.DeviceDto;
import com.ptit.iot.dto.MqttDeviceCommand;
import com.ptit.iot.dto.MqttDeviceResponse;
import com.ptit.iot.entity.ActionHistory;
import com.ptit.iot.entity.Device;
import com.ptit.iot.entity.Sensor;
import com.ptit.iot.entity.User;
import com.ptit.iot.repository.ActionHistoryRepository;
import com.ptit.iot.repository.DeviceRepository;
import com.ptit.iot.repository.SensorRepository;
import com.ptit.iot.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final SensorRepository sensorRepository;
    private final ActionHistoryRepository actionHistoryRepository;
    private final UserRepository userRepository;
    private final DeviceCodeMapper deviceCodeMapper;
    private final DtoMapper dtoMapper;
    private final MqttGateway mqttGateway;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${mqtt.topics.device-control}")
    private String deviceControlTopic;

    public DeviceService(
        DeviceRepository deviceRepository,
        SensorRepository sensorRepository,
        ActionHistoryRepository actionHistoryRepository,
        UserRepository userRepository,
        DeviceCodeMapper deviceCodeMapper,
        DtoMapper dtoMapper,
        MqttGateway mqttGateway,
        ObjectMapper objectMapper,
        SimpMessagingTemplate messagingTemplate
    ) {
        this.deviceRepository = deviceRepository;
        this.sensorRepository = sensorRepository;
        this.actionHistoryRepository = actionHistoryRepository;
        this.userRepository = userRepository;
        this.deviceCodeMapper = deviceCodeMapper;
        this.dtoMapper = dtoMapper;
        this.mqttGateway = mqttGateway;
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional(readOnly = true)
    public List<DeviceDto> listDevices() {
        return deviceRepository.findAll().stream()
            .sorted(Comparator.comparing(Device::getId))
            .map(device -> dtoMapper.toDeviceDto(device, deviceCodeMapper.toDeviceCode(device.getId()), latestStatus(device.getId())))
            .toList();
    }

    @Transactional
    public DeviceControlResponse control(DeviceControlRequest request, Authentication authentication) {
        String action = normalizeAction(request.action());
        if (request.deviceId() == null && request.deviceCode() != null && deviceCodeMapper.isAll(request.deviceCode())) {
            return controlAll(action, authentication);
        }

        Integer deviceId = request.deviceId() != null ? request.deviceId() : deviceCodeMapper.toDeviceId(request.deviceCode());
        Device device = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));
        Sensor sensor = sensorRepository.findById(deviceId).orElse(null);
        User user = resolveUser(authentication);

        ActionHistory history = new ActionHistory();
        history.setUser(user);
        history.setSensor(sensor);
        history.setDevice(device);
        history.setAction(action);
        history.setStatus("Loading");
        history.setCreatedAt(LocalDateTime.now().withNano(0));
        ActionHistory saved = actionHistoryRepository.save(history);

        String deviceCode = deviceCodeMapper.toDeviceCode(deviceId);
        publishCommand(deviceCode, action);
        return new DeviceControlResponse(saved.getId(), deviceId, deviceCode, device.getName(), action, action, saved.getCreatedAt());
    }

    @Transactional
    public DeviceControlResponse applyDeviceResponse(MqttDeviceResponse response) {
        String command = normalizeAction(response.command());
        String ack = response.status() == null ? "FAILED" : response.status().trim().toUpperCase();
        Integer deviceId = deviceCodeMapper.toDeviceId(response.deviceCode());
        ActionHistory history = actionHistoryRepository.findFirstByDeviceIdAndStatusOrderByCreatedAtDesc(deviceId, "Loading")
            .orElseThrow(() -> new IllegalArgumentException("No loading action found for device: " + response.deviceCode()));
        history.setStatus("SUCCESS".equals(ack) ? command : "Failed");
        ActionHistory saved = actionHistoryRepository.save(history);

        DeviceControlResponse wsPayload = new DeviceControlResponse(
            saved.getId(),
            saved.getDevice().getId(),
            deviceCodeMapper.toDeviceCode(saved.getDevice().getId()),
            saved.getDevice().getName(),
            saved.getAction(),
            saved.getStatus(),
            saved.getCreatedAt()
        );
        messagingTemplate.convertAndSend("/topic/device-response", wsPayload);
        return wsPayload;
    }

    private DeviceControlResponse controlAll(String action, Authentication authentication) {
        List<Device> devices = deviceRepository.findAll().stream()
            .sorted(Comparator.comparing(Device::getId))
            .toList();
        User user = resolveUser(authentication);
        LocalDateTime now = LocalDateTime.now().withNano(0);
        List<ActionHistory> histories = devices.stream().map(device -> {
            ActionHistory history = new ActionHistory();
            history.setUser(user);
            history.setSensor(sensorRepository.findById(device.getId()).orElse(null));
            history.setDevice(device);
            history.setAction(action);
            history.setStatus("Loading");
            history.setCreatedAt(now);
            return history;
        }).toList();
        List<ActionHistory> saved = actionHistoryRepository.saveAll(histories);
        publishCommand("ALL", action);
        ActionHistory first = saved.get(0);
        return new DeviceControlResponse(first.getId(), null, "ALL", "All devices", action, action, now);
    }

    private void publishCommand(String deviceCode, String action) {
        try {
            mqttGateway.sendToMqtt(objectMapper.writeValueAsString(new MqttDeviceCommand(deviceCode, action)), deviceControlTopic);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Cannot serialize MQTT command", ex);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Cannot publish MQTT command. Check MQTT broker connection: " + deviceControlTopic, ex);
        }
    }

    private User resolveUser(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName()).orElse(null);
    }

    private String latestStatus(Integer deviceId) {
        return actionHistoryRepository.findAll((root, query, cb) -> cb.equal(root.get("device").get("id"), deviceId)).stream()
            .max(Comparator.comparing(ActionHistory::getCreatedAt))
            .map(ActionHistory::getStatus)
            .filter(status -> !"Loading".equalsIgnoreCase(status))
            .orElse("OFF");
    }

    private String normalizeAction(String action) {
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Action must be ON or OFF");
        }
        String normalized = action.trim().toUpperCase();
        if (!"ON".equals(normalized) && !"OFF".equals(normalized)) {
            throw new IllegalArgumentException("Action must be ON or OFF");
        }
        return normalized;
    }
}
