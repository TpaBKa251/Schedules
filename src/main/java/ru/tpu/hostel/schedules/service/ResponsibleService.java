package ru.tpu.hostel.schedules.service;

import ru.tpu.hostel.schedules.dto.request.ResponsibleSetDto;
import ru.tpu.hostel.schedules.dto.response.ResponsibleResponseDto;
import ru.tpu.hostel.schedules.dto.response.UserShortResponseDto;
import ru.tpu.hostel.schedules.enums.EventType;

import java.time.LocalDate;
import java.util.UUID;

public interface ResponsibleService {
    ResponsibleResponseDto setResponsible(ResponsibleSetDto responsibleSetDto);

    ResponsibleResponseDto setYourselfResponsible(ResponsibleSetDto responsibleSetDto);

    UserShortResponseDto getResponsible(LocalDate date, EventType type);
}
