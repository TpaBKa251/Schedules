package ru.tpu.hostel.schedules.facade.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tpu.hostel.schedules.dto.request.ResponsibleSetDto;
import ru.tpu.hostel.schedules.dto.response.ResponsibleResponseDto;
import ru.tpu.hostel.schedules.enums.EventType;
import ru.tpu.hostel.schedules.facade.ResponsibleFacade;
import ru.tpu.hostel.schedules.service.ResponsibleService;
import ru.tpu.hostel.schedules.service.responsible.assigner.ResponsibleAssigner;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ResponsibleFacadeImpl implements ResponsibleFacade {

    private final ResponsibleService responsibleService;

    private final Set<ResponsibleAssigner> responsibleAssigners;

    @Override
    public ResponsibleResponseDto setResponsible(ResponsibleSetDto responsibleSetDto) {
        return null;
    }

    @Override
    public List<ResponsibleResponseDto> getResponsible(LocalDate date, EventType eventType) {
        return List.of();
    }

}
