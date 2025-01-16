package ru.tpu.hostel.schedules.repository;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.tpu.hostel.schedules.entity.KitchenSchedule;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KitchenSchedulesRepository extends JpaRepository<KitchenSchedule, UUID> {

    @Query(value = "SELECT k.date FROM schedules.kitchen k WHERE SUBSTRING(k.room_number from 1 for 1) LIKE CAST(:floor AS TEXT) ORDER BY k.date DESC LIMIT 1", nativeQuery = true)
    Optional<LocalDate> findLastDateOfScheduleByFloor(@Param("floor") @Min(2) @Max(5) int floor);
}
