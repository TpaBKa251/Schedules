package ru.tpu.hostel.schedules.facade;

import ru.tpu.hostel.schedules.dto.request.ResponsibleSetDto;
import ru.tpu.hostel.schedules.dto.response.ResponsibleResponseDto;
import ru.tpu.hostel.schedules.enums.EventType;

import java.time.LocalDate;
import java.util.List;

public interface ResponsibleFacade {

    ResponsibleResponseDto setResponsible(ResponsibleSetDto responsibleSetDto);

    List<ResponsibleResponseDto> getResponsible(LocalDate date, EventType eventType);

}
