package ru.tpu.hostel.schedules.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record KitchenScheduleShortResponseDto(
        UUID id,
        LocalDate date,
        String roomNumber,
        boolean checked
) {
}
