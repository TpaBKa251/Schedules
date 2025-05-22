package ru.tpu.hostel.schedules.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.tpu.hostel.internal.utils.TimeUtil;
import ru.tpu.hostel.schedules.dto.response.ActiveEventResponseDto;
import ru.tpu.hostel.schedules.dto.response.ResponsibleResponseDto;
import ru.tpu.hostel.schedules.dto.response.UserNameWithIdResponse;
import ru.tpu.hostel.schedules.entity.Responsible;

import java.util.UUID;

@Mapper(componentModel = "spring", imports = {TimeUtil.class})
public interface ResponsibleMapper {

    ResponsibleResponseDto mapToResponsibleResponseDto(Responsible responsible);

    @Mapping(target = "id", expression = "java(responsibleId)")
    @Mapping(target = "firstName", expression = "java(user.firstName() == null ? \"\" : user.firstName())")
    @Mapping(target = "lastName", expression = "java(user.lastName() == null ? \"\" : user.lastName())")
    @Mapping(target = "middleName", expression = "java(user.middleName() == null ? \"\" : user.middleName())")
    UserNameWithIdResponse mapToUserNameWithIdResponse(
            UUID responsibleId,
            UserNameWithIdResponse user
    );

    @Mapping(target = "startTime", expression = "java(responsible.getDate().atTime(0, 0))")
    @Mapping(target = "endTime", expression = "java(responsible.getDate().atTime(23, 59))")
    @Mapping(target = "status", expression = "java(TimeUtil.now().toLocalDate().equals(responsible.getDate()) "
            + "? \"IN_PROGRESS\" : \"BOOKED\")")
    @Mapping(target = "type", expression = "java(\"Ответственный \" + responsible.getType().getEventTypeName())")
    ActiveEventResponseDto mapToActiveEventResponseDto(Responsible responsible);

}
