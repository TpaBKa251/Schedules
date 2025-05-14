package ru.tpu.hostel.schedules.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.entity.Timeslot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeslotRepository extends JpaRepository<Timeslot, UUID> {

    @Query("""
            SELECT t FROM Timeslot t
            WHERE t.type = :eventType
            ORDER BY t.startTime DESC LIMIT 1
            """)
    Optional<Timeslot> findLastByType(@Param("eventType") EventType eventType);

    List<Timeslot> findByType(EventType eventType);

    List<Timeslot> findAllByTypeAndStartTimeAfter(EventType eventType, LocalDateTime startTime);

    @Query("""
            SELECT EXISTS (
                SELECT 1
                FROM Timeslot t
                WHERE t.type = :type
                  AND FUNCTION('DATE_TRUNC', 'day', t.startTime) = :date
            )
            """)
    boolean existsByTypeAndDate(@Param("type") EventType type, @Param("date") LocalDate date);

    @Query(value = """
            SELECT t
                FROM Timeslot t
                WHERE t.type = :type
                    AND FUNCTION('DATE_TRUNC', 'day', t.startTime) = :date
                    AND t.startTime >= :currentTime
                    AND t.bookingCount < t.limit
            """)
    List<Timeslot> findAllAvailableTimeslotsOnDay(
            @Param("type") EventType type,
            @Param("date") LocalDate date,
            @Param("currentTime") LocalDateTime currentTime
    );

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                SELECT ts
                FROM Timeslot ts
                WHERE ts.id = :id AND ts.bookingCount < ts.limit AND ts.startTime >= :currentTime
            """)
    Optional<Timeslot> findAvailableSlotForUpdate(
            @Param("id") UUID id,
            @Param("currentTime") LocalDateTime currentTime
    );

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                SELECT ts
                FROM Timeslot ts
                WHERE ts.id = :id AND ts.bookingCount > 0
            """)
    Optional<Timeslot> findSlotForUpdate(@Param("id") UUID id);

}
