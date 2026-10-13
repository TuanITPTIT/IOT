package com.ptit.iot.repository;

import com.ptit.iot.entity.ActionHistory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ActionHistoryRepository extends JpaRepository<ActionHistory, Long>, JpaSpecificationExecutor<ActionHistory> {
    Optional<ActionHistory> findFirstByDeviceIdAndStatusOrderByCreatedAtDesc(Integer deviceId, String status);
}
