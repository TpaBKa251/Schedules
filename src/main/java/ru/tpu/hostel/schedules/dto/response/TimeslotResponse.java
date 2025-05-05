package ru.tpu.hostel.schedules.dto.response;

import java.util.UUID;

public record TimeslotResponse(
        UUID id,
        String time
) {
}
