package ru.tpu.hostel.schedules.service.responsible.assigner.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tpu.hostel.schedules.dto.request.ResponsibleSetRequestDto;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.service.responsible.assigner.ResponsibleAssignChecker;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class DefaultResponsibleAssignChecker implements ResponsibleAssignChecker {

    private static final Set<EventType> APPLICABLE_EVENT_TYPES = Set.of(
            EventType.SOOP
    );

    @Override
    public void canAssignResponsible(ResponsibleSetRequestDto responsibleSetRequestDto) {
        // Пока не реализован (нужно ли?)
    }

    @Override
    public boolean isApplicable(EventType eventType) {
        return APPLICABLE_EVENT_TYPES.contains(eventType);
    }

}
