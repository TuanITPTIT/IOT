package com.ptit.iot.repository;

import com.ptit.iot.entity.DataSensor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DataSensorRepository extends JpaRepository<DataSensor, Long>, JpaSpecificationExecutor<DataSensor> {
    Optional<DataSensor> findFirstBySensorIdOrderByTimeDesc(Integer sensorId);

    @Query("""
        select distinct d.time
        from DataSensor d
        order by d.time desc
    """)
    List<LocalDateTime> findLatestDistinctTimes(Pageable pageable);

    List<DataSensor> findByTimeInOrderByTimeAscSensorIdAsc(List<LocalDateTime> times);

    @Query("""
        select d
        from DataSensor d
        join fetch d.sensor
        left join fetch d.device
        where d.time in :times
        order by d.time asc, d.sensor.id asc
    """)
    List<DataSensor> findFullByTimeIn(@Param("times") List<LocalDateTime> times);
}
