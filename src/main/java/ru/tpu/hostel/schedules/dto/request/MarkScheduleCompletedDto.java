package ru.tpu.hostel.schedules.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MarkScheduleCompletedDto(
        @NotBlank(message = "Номер комнаты не может быть пустым")
        String roomNumber,

        @NotNull(message = "Дата не может быть пустой")
        LocalDate date,

        @NotNull(message = "Статус не может быть пустым")
        Boolean completed
) {
}
