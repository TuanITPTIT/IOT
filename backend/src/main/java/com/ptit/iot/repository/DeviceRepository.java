package com.ptit.iot.repository;

import com.ptit.iot.entity.Device;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Integer> {
    Optional<Device> findByNameIgnoreCase(String name);
}
