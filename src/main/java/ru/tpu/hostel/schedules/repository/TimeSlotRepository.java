package ru.tpu.hostel.schedules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.tpu.hostel.schedules.entity.TimeSlot;
import ru.tpu.hostel.schedules.enums.EventType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID> {

    @Query
    Optional<TimeSlot> findLastByType(EventType eventType);

    List<TimeSlot> findByType(EventType eventType);

    List<TimeSlot> findAllByTypeAndStartTimeAfter(EventType eventType, LocalDateTime startTime);

    @Query
    Optional<LocalDateTime> findOneByTypeAndStartTimeOnSpecificDay(
            @Param("type") EventType eventType,
            @Param("startDate") LocalDateTime startTime,
            @Param("endDate") LocalDate endDate);
}
