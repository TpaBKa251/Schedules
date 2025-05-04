package ru.tpu.hostel.schedules.service.responsible.assigner.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tpu.hostel.schedules.client.UserServiceClient;
import ru.tpu.hostel.schedules.dto.request.ResponsibleSetDto;
import ru.tpu.hostel.schedules.dto.response.ResponsibleResponseDto;
import ru.tpu.hostel.schedules.enums.EventType;
import ru.tpu.hostel.schedules.service.ResponsibleService;
import ru.tpu.hostel.schedules.service.TimeSlotService;
import ru.tpu.hostel.schedules.service.responsible.assigner.CommonResponsibleAssigner;
import ru.tpu.hostel.schedules.service.responsible.assigner.ResponsibleAssigner;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class DefaultResponsibleAssigner implements ResponsibleAssigner {

    private static final Set<EventType> APPLICABLE_EVENT_TYPES = Set.of(
            EventType.SOOP
    );

    private final UserServiceClient userServiceClient;

    private final ResponsibleService responsibleService;

    private final CommonResponsibleAssigner commonResponsibleAssigner;

    @Override
    public ResponsibleResponseDto assignResponsible(ResponsibleSetDto responsibleSetDto) {
        return null;
    }

    @Override
    public boolean isApplicable(EventType eventType) {
        return APPLICABLE_EVENT_TYPES.contains(eventType);
    }

}
