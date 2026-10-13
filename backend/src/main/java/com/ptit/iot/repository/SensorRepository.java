package com.ptit.iot.repository;

import com.ptit.iot.entity.Sensor;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<Sensor, Integer> {
    Optional<Sensor> findByNameIgnoreCase(String name);
}
