package ru.tpu.hostel.schedules.repository;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;
import ru.tpu.hostel.schedules.entity.KitchenSchedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Validated
public interface KitchenSchedulesRepository extends JpaRepository<KitchenSchedule, UUID> {

    @Query(value = """
            SELECT k.date FROM schedules.kitchen k 
            WHERE SUBSTRING(k.room_number from 1 for 1) 
            LIKE CAST(:floor AS TEXT) 
            ORDER BY k.date DESC LIMIT 1""",
            nativeQuery = true)
    Optional<LocalDate> findLastDateOfScheduleByFloor(@Param("floor") @Min(2) @Max(5) int floor);

    @Query(value = """
            SELECT k.schedule_number FROM schedules.kitchen k
            WHERE SUBSTRING(k.room_number from 1 for 1)
            LIKE CAST(:floor AS TEXT)
            ORDER BY k.date DESC LIMIT 1""",
            nativeQuery = true)
    Optional<Integer> findLastNumberOfScheduleByFloor(@Param("floor") @Min(2) @Max(5) int floor);

    List<KitchenSchedule> findAllByRoomNumberAndDateLessThanEqual(String roomNumber, LocalDate date);

    List<KitchenSchedule> deleteAllByDateLessThan(LocalDate date);

    @Query("select k.date from KitchenSchedule k where k.roomNumber like(:roomNumber) order by k.date limit 1")
    Optional<LocalDate> findDateByRoomNumber(@Param("roomNumber") String roomNumber);

    @Query(value = """
            SELECT * FROM schedules.kitchen k
            WHERE SUBSTRING(k.room_number from 1 for 1)
            LIKE :floor
            ORDER BY k.date""",
            nativeQuery = true)
    Page<KitchenSchedule> findAllOnFloor(
            @Param("floor")
            @Pattern(regexp = "\\d", message = "Этаж должен быть одной цифрой")
            String floor,
            Pageable pageable
    );

    @Query(value = """
            SELECT k.room_number FROM schedules.kitchen k
            WHERE SUBSTRING(k.room_number from 1 for 1)
            LIKE CAST(:floor AS TEXT)
            ORDER BY k.room_number""",
            nativeQuery = true)
    Page<String> findAllRoomsOnFloor(@Param("floor") String floor, Pageable pageable);

    @Query(value = """
            SELECT * FROM schedules.kitchen k
            WHERE SUBSTRING(k.room_number from 1 for 1)
            LIKE CAST(:floor AS TEXT)
            and k.date = :date
            ORDER BY k.room_number""",
            nativeQuery = true)
    Optional<KitchenSchedule> findByRoomNumberAndDate(
            @Param("floor")
            @Pattern(regexp = "\\d", message = "Этаж должен быть одной цифрой")
            String floor,
            LocalDate date
    );

    List<KitchenSchedule> findAllByDateEquals(LocalDate date);

    @Query(value = """
            SELECT k.room_number FROM schedules.kitchen k
            WHERE SUBSTRING(k.room_number from 1 for 1)
            LIKE CAST(:floor AS TEXT)
            and k.date = :date
            ORDER BY k.room_number""",
            nativeQuery = true)
    Optional<String> findRoomNumberByDateAndFloor(
            @Param("floor")
            @Pattern(regexp = "\\d", message = "Этаж должен быть одной цифрой")
            String floor,
            LocalDate date
    );
}
