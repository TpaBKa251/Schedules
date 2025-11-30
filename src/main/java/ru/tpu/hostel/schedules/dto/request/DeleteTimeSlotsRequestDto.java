package ru.tpu.hostel.schedules.dto.request;

import java.util.List;
import java.util.UUID;

public record DeleteTimeSlotsRequestDto(
        List<UUID> timeSlotIds
) {
}
