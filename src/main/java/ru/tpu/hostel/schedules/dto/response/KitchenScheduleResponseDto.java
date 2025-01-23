package ru.tpu.hostel.schedules.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record KitchenScheduleResponseDto(
        UUID id,
        LocalDate date,
        String roomNumber,
        boolean checked,
        List<UserShortResponseDto> users
) {
}