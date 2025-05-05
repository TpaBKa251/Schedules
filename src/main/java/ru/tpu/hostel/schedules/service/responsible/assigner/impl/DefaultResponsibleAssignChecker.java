package ru.tpu.hostel.schedules.service.responsible.assigner.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tpu.hostel.schedules.external.rest.user.UserServiceClient;
import ru.tpu.hostel.schedules.dto.request.ResponsibleSetRequestDto;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.repository.ResponsibleRepository;
import ru.tpu.hostel.schedules.service.ResponsibleService;
import ru.tpu.hostel.schedules.service.responsible.assigner.ResponsibleAssignChecker;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class DefaultResponsibleAssignChecker implements ResponsibleAssignChecker {

    private static final Set<EventType> APPLICABLE_EVENT_TYPES = Set.of(
            EventType.SOOP
    );

    private final ResponsibleRepository responsibleRepository;

    @Override
    public void canAssignResponsible(ResponsibleSetRequestDto responsibleSetRequestDto) {
    }

    @Override
    public boolean isApplicable(EventType eventType) {
        return APPLICABLE_EVENT_TYPES.contains(eventType);
    }

}
