package ru.tpu.hostel.schedules.service.responsible.assigner.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tpu.hostel.internal.exception.ServiceException;
import ru.tpu.hostel.schedules.dto.request.ResponsibleSetRequestDto;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.repository.TimeslotRepository;
import ru.tpu.hostel.schedules.service.responsible.assigner.ResponsibleAssignChecker;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TimeslotResponsibleAssignChecker implements ResponsibleAssignChecker {

    private static final Set<EventType> APPLICABLE_EVENT_TYPES = Set.of(
            EventType.GYM
    );

    private final TimeslotRepository timeSlotRepository;

    @Transactional(readOnly = true)
    @Override
    public void canAssignResponsible(ResponsibleSetRequestDto responsibleSetRequestDto) {
        LocalDateTime dayStart = responsibleSetRequestDto.date().atStartOfDay();
        if (!timeSlotRepository.existsByTypeAndDate(
                responsibleSetRequestDto.type(),
                dayStart,
                dayStart.plusDays(1)
        )) {
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
