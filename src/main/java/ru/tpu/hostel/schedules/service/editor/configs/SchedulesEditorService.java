package ru.tpu.hostel.schedules.service.editor.configs;

import ru.tpu.hostel.schedules.dto.request.ChangeSchedulesRequestDto;
import ru.tpu.hostel.schedules.dto.request.DeleteTimeSlotsRequestDto;
import ru.tpu.hostel.schedules.entity.EventType;

public interface SchedulesEditorService {
    void editSchedule(ChangeSchedulesRequestDto changeSchedulesRequestDto, EventType eventType);
    void deleteTimeSlots(DeleteTimeSlotsRequestDto deleteTimeSlotsRequestDto);
}
