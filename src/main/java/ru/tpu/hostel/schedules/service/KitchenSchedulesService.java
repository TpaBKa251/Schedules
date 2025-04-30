package ru.tpu.hostel.schedules.service;

import ru.tpu.hostel.schedules.dto.request.MarkScheduleCompletedDto;
import ru.tpu.hostel.schedules.dto.request.SwapRequestDto;
import ru.tpu.hostel.schedules.dto.response.KitchenScheduleResponseDto;
import ru.tpu.hostel.schedules.dto.response.ActiveEventResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface KitchenSchedulesService {

    List<KitchenScheduleResponseDto> getKitchenSchedule();

    List<ActiveEventResponseDto> getActiveEvent(UUID userId);

    KitchenScheduleResponseDto getKitchenScheduleOnDate(LocalDate date);
    KitchenScheduleResponseDto getKitchenScheduleOnDate(LocalDate date, UUID userId);

    KitchenScheduleResponseDto getKitchenScheduleById(UUID userId);

    void swap(SwapRequestDto swapRequestDto);

    void markScheduleCompleted(MarkScheduleCompletedDto markScheduleCompletedDto);
}
