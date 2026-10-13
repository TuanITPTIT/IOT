package com.ptit.iot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptit.iot.dto.MqttDeviceResponse;
import com.ptit.iot.dto.MqttSensorPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class MqttMessageReceiver {
    private static final Logger log = LoggerFactory.getLogger(MqttMessageReceiver.class);

    private final ObjectMapper objectMapper;
    private final SensorService sensorService;
    private final DeviceService deviceService;

    public MqttMessageReceiver(ObjectMapper objectMapper, SensorService sensorService, DeviceService deviceService) {
        this.objectMapper = objectMapper;
        this.sensorService = sensorService;
        this.deviceService = deviceService;
    }

    @Value("${mqtt.topics.sensor-data}")
    private String sensorDataTopic;

    @Value("${mqtt.topics.device-response}")
    private String deviceResponseTopic;

    @ServiceActivator(inputChannel = "mqttInboundChannel")
    public void handleMessage(Message<?> message) {
        String topic = String.valueOf(message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC));
        String payload = String.valueOf(message.getPayload());
        try {
            if (sensorDataTopic.equals(topic)) {
                MqttSensorPayload sensorPayload = objectMapper.readValue(payload, MqttSensorPayload.class);
                sensorService.saveSensorPacket(sensorPayload.temperature(), sensorPayload.humidity(), sensorPayload.light());
            } else if (deviceResponseTopic.equals(topic)) {
                MqttDeviceResponse deviceResponse = objectMapper.readValue(payload, MqttDeviceResponse.class);
                deviceService.applyDeviceResponse(deviceResponse);
            }
        } catch (Exception ex) {
            log.error("Cannot handle MQTT message from topic {} with payload {}", topic, payload, ex);
        }
    }
}
