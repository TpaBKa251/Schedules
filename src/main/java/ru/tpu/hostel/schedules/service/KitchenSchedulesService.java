package ru.tpu.hostel.schedules.service;

import ru.tpu.hostel.schedules.dto.response.ActiveEventDto;
import ru.tpu.hostel.schedules.dto.response.KitchenScheduleResponseDto;
import ru.tpu.hostel.schedules.entity.KitchenSchedule;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface KitchenSchedulesService {

    List<KitchenScheduleResponseDto> getKitchenSchedule(UUID userId, int page, int size);

    List<ActiveEventDto> getActiveEvent(UUID userId);

    KitchenScheduleResponseDto getKitchenScheduleOnDate(LocalDate date, UUID userId);
}
