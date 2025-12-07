package ru.tpu.hostel.schedules.service.editor.configs;

import ru.tpu.hostel.schedules.dto.SchedulesDto;
import ru.tpu.hostel.schedules.entity.EventType;

public interface SchedulesEditorService {
    void editSchedule(SchedulesDto schedulesDto, EventType eventType);

    SchedulesDto getSchedule(EventType eventType);
}
