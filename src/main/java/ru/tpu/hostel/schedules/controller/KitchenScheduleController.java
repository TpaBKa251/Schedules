package ru.tpu.hostel.schedules.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.tpu.hostel.schedules.dto.request.SwapRequestDto;
import ru.tpu.hostel.schedules.dto.response.ActiveEventResponseDto;
import ru.tpu.hostel.schedules.dto.response.KitchenScheduleResponseDto;
import ru.tpu.hostel.schedules.dto.response.KitchenScheduleShortResponseDto;
import ru.tpu.hostel.schedules.service.KitchenSchedulesService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class KitchenScheduleController {

    private final KitchenSchedulesService kitchenSchedulesService;

    @GetMapping("/kitchen/get/on/floor")
    public List<KitchenScheduleShortResponseDto> getSchedule(
            @RequestParam(name = "floor", required = false) String floor
    ) {
        if (floor == null || floor.isEmpty()) {
            return kitchenSchedulesService.getSchedule();
        }
        return kitchenSchedulesService.getSchedule(floor);
    }

    @GetMapping("/kitchen/get/on/room/{identifier}")
    public List<ActiveEventResponseDto> getActiveDuties(@PathVariable("identifier") String identifier) {
        UUID userId;
        try {
            userId = UUID.fromString(identifier);
        } catch (IllegalArgumentException e) {
            return kitchenSchedulesService.getActiveDuties(identifier);
        }
        return kitchenSchedulesService.getActiveDuties(userId);
    }

    @GetMapping("/kitchen/get/on/floor/date/{date}")
    public KitchenScheduleResponseDto getDutyOnDate(
            @PathVariable("date") LocalDate date,
            @RequestParam(name = "floor", required = false) String floor
    ) {
        return kitchenSchedulesService.getDutyOnDate(date, floor);
    }

    @GetMapping("/kitchen")
    public KitchenScheduleResponseDto getDutyById(
            @RequestParam("kitchenScheduleId") UUID kitchenScheduleId
    ) {
        return kitchenSchedulesService.getDutyById(kitchenScheduleId);
    }

    @PatchMapping("/kitchen/swap")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void swapDuties(@Valid @RequestBody SwapRequestDto swapRequestDto) {
        kitchenSchedulesService.swapDuties(swapRequestDto);
    }

    @PatchMapping("/kitchen/mark/{kitchenScheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markDuty(@PathVariable UUID kitchenScheduleId) {
        kitchenSchedulesService.markDuty(kitchenScheduleId);
    }

    @DeleteMapping("/kitchen/{kitchenScheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDuty(@PathVariable UUID kitchenScheduleId) {
        kitchenSchedulesService.deleteDutyById(kitchenScheduleId);
    }
}
