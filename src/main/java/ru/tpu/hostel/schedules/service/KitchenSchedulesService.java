package ru.tpu.hostel.schedules.service;

import ru.tpu.hostel.schedules.dto.response.ActiveEventResponseDto;
import ru.tpu.hostel.schedules.dto.response.KitchenScheduleResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface KitchenSchedulesService {

    List<KitchenScheduleResponseDto> getKitchenSchedule(int page, int size);

    List<ActiveEventResponseDto> getActiveEvent(UUID userId);

    KitchenScheduleResponseDto getKitchenScheduleOnDate(LocalDate date);
}
