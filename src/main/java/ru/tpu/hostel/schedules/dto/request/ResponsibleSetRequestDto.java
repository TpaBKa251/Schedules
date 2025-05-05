package ru.tpu.hostel.schedules.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import ru.tpu.hostel.schedules.entity.EventType;

import java.time.LocalDate;
import java.util.UUID;

public record ResponsibleSetRequestDto(
        @NotNull(message = "Дата назначения ответственного не может быть пустой")
        @FutureOrPresent(message = "Дата назначения ответственного не может быть в прошлом")
        LocalDate date,

        @NotNull(message = "Тип ответственного не может быть пустым")
        EventType type,

        UUID user
) {
}