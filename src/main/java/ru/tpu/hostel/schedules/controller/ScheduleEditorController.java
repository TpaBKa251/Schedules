package ru.tpu.hostel.schedules.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tpu.hostel.schedules.dto.request.ChangeSchedulesRequestDto;
import ru.tpu.hostel.schedules.dto.request.DeleteTimeSlotsRequestDto;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.service.editor.configs.SchedulesEditorService;

@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleEditorController {

    private final SchedulesEditorService schedulesEditorService;

    @PostMapping("/edit/{eventType}")
    public void editSchedule(
            @RequestBody ChangeSchedulesRequestDto changeSchedulesRequestDto,
            @PathVariable EventType eventType
    ) {
        schedulesEditorService.editSchedule(changeSchedulesRequestDto, eventType);
    }

    @DeleteMapping("/edit/delete")
    public void deleteTimeSlot(@RequestBody DeleteTimeSlotsRequestDto deleteTimeSlotsRequestDto)
    {
        schedulesEditorService.deleteTimeSlots(deleteTimeSlotsRequestDto);
    }
}
