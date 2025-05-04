package ru.tpu.hostel.schedules.dto.request;

import jakarta.validation.constraints.NotNull;
import ru.tpu.hostel.schedules.enums.EventType;

import java.time.LocalDate;
import java.util.UUID;

public record ResponsibleSetDto(
        @NotNull(message = "Дата назначения ответственного не может быть пустой")
        LocalDate date,

        @NotNull(message = "Тип ответственного не может быть пустым")
        EventType type,

        UUID user
) {
}