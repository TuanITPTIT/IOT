package com.ptit.iot.service;

import com.ptit.iot.dto.ActionHistoryDto;
import com.ptit.iot.dto.DataSensorDto;
import com.ptit.iot.dto.DeviceDto;
import com.ptit.iot.entity.ActionHistory;
import com.ptit.iot.entity.DataSensor;
import com.ptit.iot.entity.Device;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {
    public DataSensorDto toDataSensorDto(DataSensor entity) {
        String sensorName = entity.getSensor().getName();
        return new DataSensorDto(
            entity.getId(),
            entity.getSensor().getId(),
            sensorName,
            entity.getDevice() == null ? null : entity.getDevice().getId(),
            entity.getDevice() == null ? null : entity.getDevice().getName(),
            entity.getValue(),
            unitOf(sensorName),
            entity.getTime()
        );
    }

    public DeviceDto toDeviceDto(Device entity, String code, String status) {
        return new DeviceDto(entity.getId(), code, entity.getName(), status);
    }

    public ActionHistoryDto toActionHistoryDto(ActionHistory entity) {
        return new ActionHistoryDto(
            entity.getId(),
            entity.getDevice().getId(),
            entity.getDevice().getName(),
            entity.getSensor() == null ? null : entity.getSensor().getId(),
            entity.getSensor() == null ? null : entity.getSensor().getName(),
            entity.getAction(),
            entity.getStatus(),
            entity.getCreatedAt()
        );
    }

    public String unitOf(String sensorName) {
        return switch (sensorName) {
            case "Temperature" -> "°C";
            case "Luminosity" -> "Lux";
            case "Humidity" -> "%";
            default -> "";
        };
    }
}
