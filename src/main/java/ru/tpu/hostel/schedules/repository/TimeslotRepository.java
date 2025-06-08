package ru.tpu.hostel.schedules.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.entity.Timeslot;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeslotRepository extends JpaRepository<Timeslot, UUID> {

    @Query("""
            SELECT t
            FROM Timeslot t
            WHERE t.type = :eventType
            ORDER BY t.startTime DESC
            LIMIT 1
            """
    )
    Optional<Timeslot> findLastByType(@Param("eventType") EventType eventType);

    @Query("""
            SELECT EXISTS (SELECT 1
                            FROM Timeslot t
                            WHERE t.type = :type
                                AND t.startTime >= :dayStart
                                AND t.startTime < :dayEnd)
            """
    )
    boolean existsByTypeAndDate(
            @Param("type") EventType type,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd
    );

    @Query(value = """
            SELECT t
            FROM Timeslot t
            WHERE t.type = :type
                AND t.startTime < :dayEnd
                AND t.startTime >= :currentTime
                AND t.bookingCount < t.limit
            """
    )
    List<Timeslot> findAllAvailableTimeslotsOnDay(
            @Param("type") EventType type,
            @Param("dayEnd") LocalDateTime dayEnd,
            @Param("currentTime") LocalDateTime currentTime
    );

    @Query("""
            SELECT ts
            FROM Timeslot ts
            WHERE ts.id = :id
                AND ts.bookingCount < ts.limit
                AND ts.startTime >= :currentTime
            """
    )
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Timeslot> findAvailableSlotForUpdate(
            @Param("id") UUID id,
            @Param("currentTime") LocalDateTime currentTime
    );

    @Query("""
            SELECT ts
            FROM Timeslot ts
            WHERE ts.id = :id
                AND ts.bookingCount > 0
            """
    )
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Timeslot> findSlotForUpdate(@Param("id") UUID id);

    List<Timeslot> findAllByStartTimeAfter(LocalDateTime startTime);

}
