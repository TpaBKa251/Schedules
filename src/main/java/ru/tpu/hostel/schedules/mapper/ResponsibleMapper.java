package ru.tpu.hostel.schedules.mapper;

import org.mapstruct.Mapper;
import ru.tpu.hostel.schedules.dto.response.ResponsibleResponseDto;
import ru.tpu.hostel.schedules.entity.Responsible;

@Mapper(componentModel = "spring")
public interface ResponsibleMapper {

    ResponsibleResponseDto mapToResponsibleResponseDto(Responsible responsible);
}
