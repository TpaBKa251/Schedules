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
    public List<KitchenScheduleShortResponseDto> getKitchenSchedule() {
        return kitchenSchedulesService.getKitchenSchedule();
    }

    @GetMapping("/kitchen/get/on/room/{userId}")
    public List<ActiveEventResponseDto> getActiveEvent(@PathVariable("userId") UUID userId) {
        return kitchenSchedulesService.getActiveEvent(userId);
    }

    @GetMapping("/kitchen/get/on/floor/date/{date}")
    public KitchenScheduleResponseDto getKitchenSchedule(
            @PathVariable("date") LocalDate date
    ) {
        return kitchenSchedulesService.getKitchenScheduleOnDate(date);
    }

    @GetMapping("/kitchen")
    public KitchenScheduleResponseDto getKitchenScheduleById(
            @RequestParam("kitchenScheduleId") UUID kitchenScheduleId
    ) {
        return kitchenSchedulesService.getKitchenScheduleById(kitchenScheduleId);
    }

    @PatchMapping("/kitchen/swap")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void swap(@Valid @RequestBody SwapRequestDto swapRequestDto) {
        kitchenSchedulesService.swap(swapRequestDto);
    }

    @PatchMapping("/kitchen/mark/{kitchenScheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markScheduleCompleted(@PathVariable UUID kitchenScheduleId) {
        kitchenSchedulesService.markSchedule(kitchenScheduleId);
    }

    @DeleteMapping("/kitchen/{kitchenScheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteKitchenSchedule(@PathVariable UUID kitchenScheduleId) {
        kitchenSchedulesService.deleteById(kitchenScheduleId);
    }
}
