package ru.tpu.hostel.schedules.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.tpu.hostel.schedules.dto.response.ActiveEventDto;
import ru.tpu.hostel.schedules.dto.response.KitchenScheduleResponseDto;
import ru.tpu.hostel.schedules.service.KitchenSchedulesService;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class KitchenScheduleController {

    private final KitchenSchedulesService kitchenSchedulesService;

    @GetMapping("/kitchen/get/on/floor/{userId}")
    public List<KitchenScheduleResponseDto> getKitchenSchedule(
            @PathVariable("userId")UUID userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        return kitchenSchedulesService.getKitchenSchedule(userId, page, size);
    }

    @GetMapping("/kitchen/get/on/room/{userId}")
    public List<ActiveEventDto> getActiveEvent(@PathVariable("userId") UUID userId) {
        return kitchenSchedulesService.getActiveEvent(userId);
    }

    @GetMapping("/kitchen/get/on/floor/date/{date}/{userId}")
    public KitchenScheduleResponseDto getKitchenSchedule(
            @PathVariable("date")LocalDate date,
            @PathVariable("userId")UUID userId
    ) {
        return kitchenSchedulesService.getKitchenScheduleOnDate(date, userId);
    }
}
