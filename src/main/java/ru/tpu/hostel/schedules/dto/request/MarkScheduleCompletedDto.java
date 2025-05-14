package ru.tpu.hostel.schedules.dto.request;

import jakarta.validation.constraints.NotNull;

public record MarkScheduleCompletedDto(
        @NotNull(message = "Статус не может быть пустым")
        Boolean completed
) {
}
