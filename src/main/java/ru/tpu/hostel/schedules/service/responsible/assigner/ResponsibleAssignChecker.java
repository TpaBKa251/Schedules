package ru.tpu.hostel.schedules.service.responsible.assigner;

import ru.tpu.hostel.schedules.dto.request.ResponsibleSetRequestDto;
import ru.tpu.hostel.schedules.entity.EventType;

public interface ResponsibleAssignChecker {

    void canAssignResponsible(ResponsibleSetRequestDto responsibleSetRequestDto);

    boolean isApplicable(EventType eventType);

}
