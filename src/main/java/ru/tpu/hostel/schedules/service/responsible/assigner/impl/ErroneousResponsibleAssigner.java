package ru.tpu.hostel.schedules.service.responsible.assigner.impl;

import org.springframework.stereotype.Service;
import ru.tpu.hostel.internal.common.exception.ServiceException;
import ru.tpu.hostel.schedules.dto.request.ResponsibleSetDto;
import ru.tpu.hostel.schedules.dto.response.ResponsibleResponseDto;
import ru.tpu.hostel.schedules.enums.EventType;
import ru.tpu.hostel.schedules.service.responsible.assigner.ResponsibleAssigner;

import java.util.Set;

@Service
public class ErroneousResponsibleAssigner implements ResponsibleAssigner {

    private static final Set<EventType> APPLICABLE_EVENT_TYPES = Set.of(
            EventType.KITCHEN
    );

    @Override
    public ResponsibleResponseDto assignResponsible(ResponsibleSetDto responsibleSetDto) {
        throw new ServiceException.MethodNotAllowed(
                "Вы не можете назначить ответственного на день для типа " + responsibleSetDto.type().getEventTypeName()
        );
    }

    @Override
    public boolean isApplicable(EventType eventType) {
        return APPLICABLE_EVENT_TYPES.contains(eventType);
    }
}
