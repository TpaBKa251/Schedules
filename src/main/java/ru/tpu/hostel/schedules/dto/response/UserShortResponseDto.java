package ru.tpu.hostel.schedules.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с краткой инфой о юзере (ФИО и ссылки на соцсети)")
public record UserShortResponseDto(

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
