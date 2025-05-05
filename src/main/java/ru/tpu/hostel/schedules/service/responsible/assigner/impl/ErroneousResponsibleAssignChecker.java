package ru.tpu.hostel.schedules.service.responsible.assigner.impl;

import org.springframework.stereotype.Service;
import ru.tpu.hostel.internal.exception.ServiceException;
import ru.tpu.hostel.schedules.dto.request.ResponsibleSetRequestDto;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.service.responsible.assigner.ResponsibleAssignChecker;

import java.util.Set;

@Service
public class ErroneousResponsibleAssignChecker implements ResponsibleAssignChecker {

    private static final Set<EventType> APPLICABLE_EVENT_TYPES = Set.of(
            EventType.KITCHEN
    );

    @Override
    public void canAssignResponsible(ResponsibleSetRequestDto responsibleSetRequestDto) {
        throw new ServiceException.NotAcceptable(
                "Вы не можете назначить ответственного на день для типа "
                        + responsibleSetRequestDto.type().getEventTypeName()
        );
    }

    @Override
    public boolean isApplicable(EventType eventType) {
        return APPLICABLE_EVENT_TYPES.contains(eventType);
    }
}
