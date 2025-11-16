package ru.tpu.hostel.schedules.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;

import java.util.UUID;

@Schema(description = "Тело запроса изменения ответственного на день за ресурс")
public record ResponsibleEditRequestDto(

        @Schema(description = "ID юзера, который будет назначен ответственным за ресурс (опционально). " +
                "Если не указать, то возьмется ID текущего юзера (отправившего запрос)")
        @Nullable
        UUID user

) {
}
