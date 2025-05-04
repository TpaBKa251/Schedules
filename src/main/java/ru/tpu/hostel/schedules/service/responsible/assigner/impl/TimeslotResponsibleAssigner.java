package ru.tpu.hostel.schedules.service.responsible.assigner.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tpu.hostel.schedules.client.UserServiceClient;
import ru.tpu.hostel.schedules.dto.request.ResponsibleSetDto;
import ru.tpu.hostel.schedules.dto.response.ResponsibleResponseDto;
import ru.tpu.hostel.schedules.entity.Responsible;
import ru.tpu.hostel.schedules.enums.EventType;
import ru.tpu.hostel.schedules.mapper.ResponsibleMapper;
import ru.tpu.hostel.schedules.repository.ResponsibleRepository;
import ru.tpu.hostel.schedules.repository.TimeSlotRepository;
import ru.tpu.hostel.schedules.service.ResponsibleService;
import ru.tpu.hostel.schedules.service.TimeSlotService;
import ru.tpu.hostel.schedules.service.responsible.assigner.CommonResponsibleAssigner;
import ru.tpu.hostel.schedules.service.responsible.assigner.ResponsibleAssigner;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TimeslotResponsibleAssigner implements ResponsibleAssigner {

    private static final Set<EventType> APPLICABLE_EVENT_TYPES = Set.of(
            EventType.HALL,
            EventType.INTERNET,
            EventType.GYM
    );

    private final UserServiceClient userServiceClient;

    private final ResponsibleService responsibleService;

    private final TimeSlotService timeSlotService;

    private final CommonResponsibleAssigner commonResponsibleAssigner;

    @Override
    public ResponsibleResponseDto assignResponsible(ResponsibleSetDto responsibleSetDto) {
        return null;
    }

    @Override
    public boolean isApplicable(EventType eventType) {
        return APPLICABLE_EVENT_TYPES.contains(eventType);
    }

    private Responsible firstSetResponsible(EventType type, ResponsibleSetDto responsibleSetDto) {
        if (timeSlotRepository.findEarlierStartTimeByTypeAndStartTimeOnSpecificDay(
                type,
                responsibleSetDto.date().atTime(0, 0),
                responsibleSetDto.date().atTime(23, 59)
        ).isPresent()) {
            var responsible = new Responsible();
            responsible.setType(type);
            responsible.setDate(responsibleSetDto.date());
            return responsible;
        } else {
            return null;
        }
    }

    private Boolean userHasResponsibleRole(EventType type, ResponsibleSetDto responsibleSetDto) {
        List<String> roles = userServiceClient.getAllRolesByUserId(responsibleSetDto.user());
        for (String role : roles) {
            if (role.contains(type.toString())) {
                return true;
            }
        }
        return false;
    }

}
