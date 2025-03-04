package ru.tpu.hostel.schedules.dto.response;

import java.util.UUID;

public record TimeSlotResponse(
        UUID id,
        String time
) {
}
