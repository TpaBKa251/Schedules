package ru.tpu.hostel.schedules.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SwapRequestDto(
        @NotNull(message = "Номер комнаты А не может быть пустым")
        String roomNumberA,
        @NotNull(message = "Дата назначения дежурства А не может быть пустой")
        LocalDate dateA,

        @NotNull(message = "Номер комнаты B не может быть пустым")
        String roomNumberB,
        @NotNull(message = "Дата назначения дежурства B не может быть пустой")
        LocalDate dateB
) {
}
