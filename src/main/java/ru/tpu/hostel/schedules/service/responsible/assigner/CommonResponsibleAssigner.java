package ru.tpu.hostel.schedules.service.responsible.assigner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tpu.hostel.schedules.dto.request.ResponsibleSetDto;
import ru.tpu.hostel.schedules.dto.response.ResponsibleResponseDto;
import ru.tpu.hostel.schedules.service.ResponsibleService;

@Service
@RequiredArgsConstructor
public class CommonResponsibleAssigner {

    private final ResponsibleService responsibleService;

    public ResponsibleResponseDto assign(ResponsibleSetDto responsibleSetDto) {
        return null;
    }

    public ResponsibleResponseDto selfAssign(ResponsibleSetDto responsibleSetDto) {
        return null;
    }

}
