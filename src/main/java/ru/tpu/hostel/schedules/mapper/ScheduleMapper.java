package ru.tpu.hostel.schedules.mapper;

import lombok.experimental.UtilityClass;
import ru.tpu.hostel.schedules.config.schedule.TimeslotSchedulesConfig;
import ru.tpu.hostel.schedules.dto.SchedulesDto;

@UtilityClass
public class ScheduleMapper {
    public static SchedulesDto mapToSchedulesDto(TimeslotSchedulesConfig.Schedule scheduleConfig) {
        return new SchedulesDto(
                scheduleConfig.getLimit(),
                scheduleConfig.getResponsible(),
                scheduleConfig.getWorkingDays(),
                scheduleConfig.getWorkingHours(),
                scheduleConfig.getSlotDurationMinutes(),
                scheduleConfig.getBreaks(),
                scheduleConfig.getReservedHours()
        );
    }
}
