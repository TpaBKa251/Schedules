package ru.tpu.hostel.schedules.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.tpu.hostel.schedules.entity.EventType;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Ответ с описанием ответственного на день за ресурс")
public record ResponsibleResponseDto(

        @Schema(description = "Дата назначения")
        LocalDate date,

        @Schema(description = "Тип ответственного")
        EventType type,

        @Schema(description = "ID юзера, который назначен")
        UUID user

) {
}