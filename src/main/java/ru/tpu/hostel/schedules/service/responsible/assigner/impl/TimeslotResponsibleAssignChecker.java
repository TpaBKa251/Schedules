package ru.tpu.hostel.schedules.service.responsible.assigner.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tpu.hostel.internal.exception.ServiceException;
import ru.tpu.hostel.schedules.dto.request.ResponsibleSetRequestDto;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.repository.TimeslotRepository;
import ru.tpu.hostel.schedules.service.responsible.assigner.ResponsibleAssignChecker;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class TimeslotResponsibleAssignChecker implements ResponsibleAssignChecker {

    private static final Set<EventType> APPLICABLE_EVENT_TYPES = Set.of(
            EventType.HALL,
            EventType.INTERNET,
            EventType.GYM
    );

    private final TimeslotRepository timeSlotRepository;

    @Override
    public void canAssignResponsible(ResponsibleSetRequestDto responsibleSetRequestDto) {
        if (!timeSlotRepository.existsByTypeAndDate(responsibleSetRequestDto.type(), responsibleSetRequestDto.date())) {
            throw new ServiceException.Conflict(
                    "Невозможно назначить ответственного по %s на %s, так как нет слотов на день".formatted(
                            responsibleSetRequestDto.type().getEventTypeName(),
                            responsibleSetRequestDto.date()
                    )
            );
        }
    }

    @Override
    public boolean isApplicable(EventType eventType) {
        return APPLICABLE_EVENT_TYPES.contains(eventType);
    }

}
