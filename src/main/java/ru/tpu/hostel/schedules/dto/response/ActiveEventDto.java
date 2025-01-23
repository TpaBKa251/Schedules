package ru.tpu.hostel.schedules.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ActiveEventDto(
        UUID id,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status,
        String type
) {}