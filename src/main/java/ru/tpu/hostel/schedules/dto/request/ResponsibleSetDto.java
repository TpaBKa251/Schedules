package ru.tpu.hostel.schedules.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ResponsibleSetDto(
        @NotNull(message = "Дата назначения ответственного не может быть пустой")
        LocalDate date,

        UUID user
) {
}