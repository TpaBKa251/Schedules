package ru.tpu.hostel.schedules.service.responsible.assigner;

import ru.tpu.hostel.schedules.dto.request.ResponsibleSetDto;
import ru.tpu.hostel.schedules.dto.response.ResponsibleResponseDto;
import ru.tpu.hostel.schedules.enums.EventType;

public interface ResponsibleAssigner {

    ResponsibleResponseDto assignResponsible(ResponsibleSetDto responsibleSetDto);

    boolean isApplicable(EventType eventType);

}
