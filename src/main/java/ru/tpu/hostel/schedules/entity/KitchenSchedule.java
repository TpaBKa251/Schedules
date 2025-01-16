package ru.tpu.hostel.schedules.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "kitchen", schema = "schedules")
@Getter
@Setter
public class KitchenSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "\"date\"", nullable = false)
    private LocalDate date;

    @Column(name = "room_number", nullable = false)
    private String roomNumber;
}
