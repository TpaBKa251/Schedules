package ru.tpu.hostel.schedules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.tpu.hostel.schedules.entity.TimeSlot;
import ru.tpu.hostel.schedules.enums.EventType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID> {

    @Query("""
            SELECT t FROM TimeSlot t
            WHERE t.type = :eventType
            ORDER BY t.startTime DESC LIMIT 1
            """)
    Optional<TimeSlot> findLastByType(@Param("eventType") EventType eventType);

    List<TimeSlot> findByType(EventType eventType);

    List<TimeSlot> findAllByTypeAndStartTimeAfter(EventType eventType, LocalDateTime startTime);

    @Query("""
            SELECT t.startTime FROM TimeSlot t
            WHERE t.type = :type
            AND t.startTime >= :startDate
            AND t.startTime < :endDate
            ORDER BY t.startTime LIMIT 1
            """)
    Optional<LocalDateTime> findEarlierStartTimeByTypeAndStartTimeOnSpecificDay(
            @Param("type") EventType eventType,
            @Param("startDate") LocalDateTime startTime,
            @Param("endDate") LocalDateTime endDate);
}
