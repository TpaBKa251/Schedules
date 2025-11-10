package ru.tpu.hostel.schedules.dto.request;

import ru.tpu.hostel.schedules.config.schedule.TimeslotSchedulesConfig;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ChangeSchedulesRequestDto(
        int limit,
        UUID responsible,
        List<String> workingDays,
        TimeslotSchedulesConfig.TimeRange workingHours,
        int slotDurationMinutes,
        TimeslotSchedulesConfig.BreakConfig breaks,
        Map<String, List<TimeslotSchedulesConfig.TimeRange>> reservedHours
) {
}
