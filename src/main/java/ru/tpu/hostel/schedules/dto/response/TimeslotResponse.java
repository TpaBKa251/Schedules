package ru.tpu.hostel.schedules.dto.response;

import java.util.UUID;

public record TimeslotResponse(
        UUID id,
        String startTime,
        String endTime,
        Integer bookingCount,
        Integer limit,
        Boolean isBookedByMe
) {
}
