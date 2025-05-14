package ru.tpu.hostel.schedules.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SwapRequestDto(

        @NotNull(message = "ID дежурства не может быть пустым")
        UUID dutyId1,

        @NotNull(message = "ID дежурства не может быть пустым")
        UUID dutyId2
) {
}
