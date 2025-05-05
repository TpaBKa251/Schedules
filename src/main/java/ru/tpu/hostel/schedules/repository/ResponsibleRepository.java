package ru.tpu.hostel.schedules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.tpu.hostel.schedules.entity.Responsible;
import ru.tpu.hostel.schedules.entity.EventType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResponsibleRepository extends JpaRepository<Responsible, UUID> {

    Optional<Responsible> findByTypeAndDate(EventType type, LocalDate date);

    List<Responsible> findAllByTypeAndDate(EventType type, LocalDate date);

    @Query("select r.user from Responsible r where r.type = :type and r.date = :date")
    Optional<UUID> findUserByTypeAndDate(@Param("type") EventType type, @Param("date") LocalDate date);

}
