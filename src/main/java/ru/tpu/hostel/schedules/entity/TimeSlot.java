package ru.tpu.hostel.schedules.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import ru.tpu.hostel.schedules.enums.EventType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "time_slots", schema = "schedules")
@Getter
@Setter
public class TimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, name = "start_time")
    private LocalDateTime startTime;

    @Column(nullable = false, name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private EventType type;

    @Column(name = "\"limit\"", nullable = false)
    private Integer limit;
}
