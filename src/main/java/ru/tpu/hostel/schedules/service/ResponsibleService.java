package ru.tpu.hostel.schedules.service;

import ru.tpu.hostel.schedules.dto.request.ResponsibleEditRequestDto;
import ru.tpu.hostel.schedules.dto.request.ResponsibleSetRequestDto;
import ru.tpu.hostel.schedules.dto.response.ResponsibleResponseDto;
import ru.tpu.hostel.schedules.dto.response.UserNameWithIdResponse;
import ru.tpu.hostel.schedules.dto.response.UserShortResponseDto;
import ru.tpu.hostel.schedules.entity.EventType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ResponsibleService {

    ResponsibleResponseDto setResponsible(ResponsibleSetRequestDto responsibleSetRequestDto);

    ResponsibleResponseDto editResponsible(UUID responsibleId, ResponsibleEditRequestDto responsibleEditRequestDto);

    UserShortResponseDto getResponsibleByTypeAndDate(LocalDate date, EventType type);

    List<UserNameWithIdResponse> getAllResponsibleByTypeAndDate(LocalDate date, EventType type);

}
