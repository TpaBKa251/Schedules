package ru.tpu.hostel.schedules.service;

import ru.tpu.hostel.schedules.dto.request.SwapRequestDto;
import ru.tpu.hostel.schedules.dto.response.ActiveEventResponseDto;
import ru.tpu.hostel.schedules.dto.response.KitchenScheduleResponseDto;
import ru.tpu.hostel.schedules.dto.response.KitchenScheduleShortResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface KitchenSchedulesService {

    List<KitchenScheduleShortResponseDto> getSchedule(String floorFromRequest);

    List<KitchenScheduleShortResponseDto> getSchedule();

    List<ActiveEventResponseDto> getActiveDuties(String roomNumber);

    List<ActiveEventResponseDto> getActiveDuties(UUID userId);

    /**
     * @deprecated заменен на {@link #getDutyById(UUID)}
     */
    @Deprecated(forRemoval = true)
    KitchenScheduleResponseDto getDutyOnDate(LocalDate date, String floorFromRequest);

    KitchenScheduleResponseDto getDutyById(UUID id);

    void swapDuties(SwapRequestDto swapRequestDto);

    void markDuty(UUID kitchenScheduleId);

    void deleteDutyById(UUID id);
}
