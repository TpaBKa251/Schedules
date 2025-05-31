package ru.tpu.hostel.schedules.dto.request;

import jakarta.annotation.Nullable;

import java.util.UUID;

public record ResponsibleEditRequestDto(
        @Nullable
        UUID user
) {
}
