package ru.tpu.hostel.schedules.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tpu.hostel.schedules.dto.request.MarkScheduleCompletedDto;
import ru.tpu.hostel.schedules.dto.request.SwapRequestDto;
import ru.tpu.hostel.schedules.dto.response.ActiveEventDto;
import ru.tpu.hostel.schedules.dto.response.KitchenScheduleResponseDto;
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
    public List<KitchenScheduleResponseDto> getKitchenSchedule() {
        return kitchenSchedulesService.getKitchenSchedule();
    }

    @GetMapping("/kitchen/get/on/room/{userId}")
    public List<ActiveEventDto> getActiveEvent(@PathVariable("userId") UUID userId) {
        return kitchenSchedulesService.getActiveEvent(userId);
    }

    @GetMapping("/kitchen/get/on/floor/date/{date}")
    public KitchenScheduleResponseDto getKitchenSchedule(
            @PathVariable("date") LocalDate date
    ) {
        return kitchenSchedulesService.getKitchenScheduleOnDate(date);
    }

    @GetMapping("/kitchen/get/{kitchenScheduleId}")
    public KitchenScheduleResponseDto getKitchenScheduleById(
            @PathVariable("kitchenScheduleId") UUID kitchenScheduleId
    ) {
        return kitchenSchedulesService.getKitchenScheduleById(kitchenScheduleId);
    }

    @PatchMapping("/kitchen/swap")
    public ResponseEntity<?> swap(@Valid @RequestBody SwapRequestDto swapRequestDto) {
        kitchenSchedulesService.swap(swapRequestDto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/kitchen/mark-completed")
    public ResponseEntity<?> markScheduleCompleted(
            @Valid @RequestBody MarkScheduleCompletedDto markScheduleCompletedDto) {
        kitchenSchedulesService.markScheduleCompleted(markScheduleCompletedDto);
        return ResponseEntity.ok().build();
    }
}
