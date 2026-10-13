package com.ptit.iot.service;

import com.ptit.iot.dto.ChartDataPoint;
import com.ptit.iot.dto.DataSensorDto;
import com.ptit.iot.dto.LatestSensorResponse;
import com.ptit.iot.dto.PageResponse;
import com.ptit.iot.entity.DataSensor;
import com.ptit.iot.entity.Device;
import com.ptit.iot.entity.Sensor;
import com.ptit.iot.repository.DataSensorRepository;
import com.ptit.iot.repository.DeviceRepository;
import com.ptit.iot.repository.SensorRepository;
import com.ptit.iot.specification.DataSensorSpecifications;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SensorService {
    private static final int TEMPERATURE_ID = 1;
    private static final int LUMINOSITY_ID = 2;
    private static final int HUMIDITY_ID = 3;
    private static final DateTimeFormatter CHART_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final DataSensorRepository dataSensorRepository;
    private final SensorRepository sensorRepository;
    private final DeviceRepository deviceRepository;
    private final DtoMapper dtoMapper;
    private final TimeParser timeParser;
    private final PageSortFactory pageSortFactory;
    private final SimpMessagingTemplate messagingTemplate;

    public SensorService(
        DataSensorRepository dataSensorRepository,
        SensorRepository sensorRepository,
        DeviceRepository deviceRepository,
        DtoMapper dtoMapper,
        TimeParser timeParser,
        PageSortFactory pageSortFactory,
        SimpMessagingTemplate messagingTemplate
    ) {
        this.dataSensorRepository = dataSensorRepository;
        this.sensorRepository = sensorRepository;
        this.deviceRepository = deviceRepository;
        this.dtoMapper = dtoMapper;
        this.timeParser = timeParser;
        this.pageSortFactory = pageSortFactory;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public LatestSensorResponse saveSensorPacket(Float temperature, Float humidity, Float light) {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        List<DataSensor> rows = new ArrayList<>();
        rows.add(newDataSensor(TEMPERATURE_ID, temperature, now));
        rows.add(newDataSensor(LUMINOSITY_ID, light, now));
        rows.add(newDataSensor(HUMIDITY_ID, humidity, now));
        dataSensorRepository.saveAll(rows);

        LatestSensorResponse latest = new LatestSensorResponse(temperature, light, humidity, now);
        messagingTemplate.convertAndSend("/topic/sensors/latest", latest);
        return latest;
    }

    @Transactional(readOnly = true)
    public LatestSensorResponse latest() {
        DataSensor temperature = latestBySensor(TEMPERATURE_ID);
        DataSensor luminosity = latestBySensor(LUMINOSITY_ID);
        DataSensor humidity = latestBySensor(HUMIDITY_ID);
        LocalDateTime timestamp = java.util.stream.Stream.of(temperature, luminosity, humidity)
            .filter(java.util.Objects::nonNull)
            .map(DataSensor::getTime)
            .max(Comparator.naturalOrder())
            .orElse(LocalDateTime.now().withNano(0));
        return new LatestSensorResponse(
            temperature == null ? 0F : temperature.getValue(),
            luminosity == null ? 0F : luminosity.getValue(),
            humidity == null ? 0F : humidity.getValue(),
            timestamp
        );
    }

    @Transactional(readOnly = true)
    public List<ChartDataPoint> chartData(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        List<LocalDateTime> latestTimes = dataSensorRepository.findLatestDistinctTimes(PageRequest.of(0, safeLimit));
        if (latestTimes.isEmpty()) {
            return List.of();
        }
        latestTimes.sort(Comparator.naturalOrder());
        List<DataSensor> rows = dataSensorRepository.findFullByTimeIn(latestTimes);
        Map<LocalDateTime, MutablePoint> grouped = new LinkedHashMap<>();
        for (LocalDateTime time : latestTimes) {
            grouped.put(time, new MutablePoint(time));
        }
        for (DataSensor row : rows) {
            MutablePoint point = grouped.get(row.getTime());
            if (point == null) {
                continue;
            }
            switch (row.getSensor().getId()) {
                case TEMPERATURE_ID -> point.temperature = row.getValue();
                case LUMINOSITY_ID -> point.luminosity = row.getValue();
                case HUMIDITY_ID -> point.humidity = row.getValue();
                default -> {
                }
            }
        }
        return grouped.values().stream()
            .map(point -> new ChartDataPoint(
                point.time.format(CHART_TIME_FORMAT),
                point.temperature,
                point.humidity,
                point.luminosity
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<DataSensorDto> history(
        String searchBy,
        String query,
        String sensorType,
        String value,
        String fromTime,
        String toTime,
        int page,
        int size,
        String sort,
        String sortBy,
        String sortDir
    ) {
        Pageable pageable = pageSortFactory.create(page, size, sort, sortBy, sortDir, "time", Set.of("id", "value", "time"));
        Page<DataSensor> result = dataSensorRepository.findAll(
            DataSensorSpecifications.filter(searchBy, query, sensorType, value, timeParser.parseStart(fromTime), timeParser.parseEnd(toTime)),
            pageable
        );
        return new PageResponse<>(
            result.getContent().stream().map(dtoMapper::toDataSensorDto).toList(),
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
    }

    private DataSensor latestBySensor(Integer sensorId) {
        return dataSensorRepository.findFirstBySensorIdOrderByTimeDesc(sensorId).orElse(null);
    }

    private DataSensor newDataSensor(Integer sensorId, Float value, LocalDateTime time) {
        Sensor sensor = sensorRepository.findById(sensorId)
            .orElseThrow(() -> new IllegalArgumentException("Sensor not found: " + sensorId));
        Device device = deviceRepository.findById(sensorId).orElse(null);
        DataSensor entity = new DataSensor();
        entity.setSensor(sensor);
        entity.setDevice(device);
        entity.setValue(value == null ? 0F : value);
        entity.setTime(time);
        return entity;
    }

    private static class MutablePoint {
        private final LocalDateTime time;
        private Float temperature = 0F;
        private Float humidity = 0F;
        private Float luminosity = 0F;

        private MutablePoint(LocalDateTime time) {
            this.time = time;
        }
    }
}
