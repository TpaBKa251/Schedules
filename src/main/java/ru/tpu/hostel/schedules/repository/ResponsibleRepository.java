package ru.tpu.hostel.schedules.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.entity.Responsible;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResponsibleRepository extends JpaRepository<Responsible, UUID> {

    Optional<Responsible> findByTypeAndDate(EventType type, LocalDate date);

    List<Responsible> findAllByTypeAndDate(EventType type, LocalDate date);

    @Query("""
            SELECT r
            FROM Responsible r
            WHERE r.id = :id
            """
    )
    @Lock(LockModeType.OPTIMISTIC)
    Optional<Responsible> findByIdOptimistic(@Param("id") UUID id);

    @Query("""
            SELECT r
            FROM Responsible r
            WHERE r.user = :user
                AND r.date >= :today
                AND r.date <= :maxDate
            """
    )
    List<Responsible> findAllActiveResponsible(
            @Param("user") UUID user,
            @Param("maxDate") LocalDate maxDate,
            @Param("today") LocalDate today
    );
}
