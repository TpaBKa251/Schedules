package ru.tpu.hostel.schedules.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import ru.tpu.hostel.schedules.entity.EventType;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Тело запроса для назначения ответственного на день за ресурс")
public record ResponsibleSetRequestDto(

        @Schema(description = "Дата дежурства (назначения ответственного)")
        @NotNull(message = "Дата назначения ответственного не может быть пустой")
        @FutureOrPresent(message = "Дата назначения ответственного не может быть в прошлом")
        LocalDate date,

        @Schema(description = "Тип ответственного")
        @NotNull(message = "Тип ответственного не может быть пустым")
        EventType type,

        @Schema(description = "ID юзера, который будет назначен (опционально). Если не указан, то берется текущий юзер (который отправил запрос)")
        UUID user

) {
}