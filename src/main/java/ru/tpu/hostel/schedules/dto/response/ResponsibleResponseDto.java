package ru.tpu.hostel.schedules.dto.response;

import ru.tpu.hostel.schedules.entity.EventType;

import java.time.LocalDate;
import java.util.UUID;

public record ResponsibleResponseDto(
        LocalDate date,
        EventType type,
        UUID user
) {
}