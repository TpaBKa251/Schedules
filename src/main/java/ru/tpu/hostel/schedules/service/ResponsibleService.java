package ru.tpu.hostel.schedules.service;

import ru.tpu.hostel.schedules.dto.request.ResponsibleSetDto;
import ru.tpu.hostel.schedules.dto.response.ResponsibleResponseDto;
import ru.tpu.hostel.schedules.dto.response.UserShortResponseDto;
import ru.tpu.hostel.schedules.enums.EventType;

import java.time.LocalDate;

public interface ResponsibleService {
    ResponsibleResponseDto setResponsible(ResponsibleSetDto responsibleSetDto);

    UserShortResponseDto getResponsible(LocalDate date, EventType type);
}
