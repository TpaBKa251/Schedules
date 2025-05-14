package ru.tpu.hostel.schedules.service;

import ru.tpu.hostel.schedules.dto.request.SwapRequestDto;
import ru.tpu.hostel.schedules.dto.response.ActiveEventResponseDto;
import ru.tpu.hostel.schedules.dto.response.KitchenScheduleResponseDto;
import ru.tpu.hostel.schedules.dto.response.KitchenScheduleShortResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface KitchenSchedulesService {

    List<KitchenScheduleShortResponseDto> getKitchenSchedule();

    List<ActiveEventResponseDto> getActiveEvent(UUID userId);

    KitchenScheduleResponseDto getKitchenScheduleOnDate(LocalDate date);

    KitchenScheduleResponseDto getKitchenScheduleById(UUID id);

    void swap(SwapRequestDto swapRequestDto);

    void markSchedule(UUID kitchenScheduleId);

    void deleteById(UUID id);
}
