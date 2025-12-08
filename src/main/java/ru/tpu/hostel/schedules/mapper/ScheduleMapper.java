package ru.tpu.hostel.schedules.mapper;

import org.mapstruct.Mapper;
import ru.tpu.hostel.schedules.config.schedule.TimeslotSchedulesConfig;
import ru.tpu.hostel.schedules.dto.SchedulesDto;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {
    SchedulesDto mapToSchedulesDto(TimeslotSchedulesConfig.Schedule scheduleConfig);
}
