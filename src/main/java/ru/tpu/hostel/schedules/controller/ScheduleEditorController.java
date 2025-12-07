package ru.tpu.hostel.schedules.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tpu.hostel.schedules.dto.SchedulesDto;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.service.editor.configs.SchedulesEditorService;

@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleEditorController {

    private final SchedulesEditorService schedulesEditorService;

    @PostMapping("/edit/{eventType}")
    public void editSchedule(
            @RequestBody SchedulesDto schedulesDto,
            @PathVariable EventType eventType
    ) {
        schedulesEditorService.editSchedule(schedulesDto, eventType);
    }

    @GetMapping("/{eventType}")
    public SchedulesDto getSchedule(@PathVariable EventType eventType) {
        return schedulesEditorService.getSchedule(eventType);
    }
}
