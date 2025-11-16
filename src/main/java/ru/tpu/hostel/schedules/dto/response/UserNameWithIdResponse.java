package ru.tpu.hostel.schedules.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Ответ с краткой инфой о юзере (ID, ФИО и ссылки на соцсети)")
public record UserNameWithIdResponse(

        @Schema(description = "ID")
        UUID id,

        @Schema(description = "Имя")
        String firstName,

        @Schema(description = "Фамилия")
        String lastName,

        @Schema(description = "Отчество")
        String middleName,

        @Schema(description = "Имя в ТГ")
        String tgLink,

        @Schema(description = "Имя в ВК")
        String vkLink

) {
}