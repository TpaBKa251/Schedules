package ru.tpu.hostel.schedules.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Ответ с активным ивентом юзера")
public record ActiveEventResponseDto(

        @Schema(description = "ID ивента")
        UUID id,

        @Schema(description = "Время старта ивента")
        LocalDateTime startTime,

        @Schema(description = "Врем конца ивента")
        LocalDateTime endTime,

        @Schema(description = "Статус ивента")
        String status,

        @Schema(description = "Тип ивента")
        String type

) {}