package ru.tpu.hostel.schedules.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tpu.hostel.schedules.client.UserServiceClient;
import ru.tpu.hostel.schedules.dto.request.ResponsibleSetDto;
import ru.tpu.hostel.schedules.dto.response.ResponsibleResponseDto;
import ru.tpu.hostel.schedules.dto.response.UserNameWithIdResponse;
import ru.tpu.hostel.schedules.dto.response.UserShortResponseDto;
import ru.tpu.hostel.schedules.entity.Responsible;
import ru.tpu.hostel.schedules.enums.EventType;
import ru.tpu.hostel.schedules.exception.ResponsibleNotFoundException;
import ru.tpu.hostel.schedules.mapper.ResponsibleMapper;
import ru.tpu.hostel.schedules.repository.ResponsibleRepository;
import ru.tpu.hostel.schedules.repository.TimeSlotRepository;
import ru.tpu.hostel.schedules.service.ResponsibleService;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResponsibleServiceImpl implements ResponsibleService {

    private final ResponsibleRepository responsibleRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final UserServiceClient userServiceClient;
    private final ResponsibleMapper responsibleMapper;

    @Override
    public ResponsibleResponseDto setResponsible(ResponsibleSetDto responsibleSetDto) {
        //Ищется чувак по типу и дате (он на какую-то дату назначен),
        // если ответственного еще нет вообще - попытка засетить его первый раз
        Responsible responsible = responsibleRepository
                .findByTypeAndDate(responsibleSetDto.type(), responsibleSetDto.date())
                .orElse(firstSetResponsible(responsibleSetDto));

        // если ответственный засетился и у юзера есть соотв. роль ответственного - можем назначить
        if (responsible != null && userHasResponsibleRole(responsibleSetDto)) {
            responsible.setUser(responsibleSetDto.user());
            responsibleRepository.save(responsible);
            return responsibleMapper.mapToResponsibleResponseDto(responsible);
        }
        else {
            throw new ResponsibleNotFoundException();
        }
    }

    //Для отображения человека с именем и ролью (Показывает, кто ответственный на день)
    @Override
    public UserShortResponseDto getResponsible(LocalDate date, EventType type) {
        //Ищется ответственный по типу и дате
        Responsible responsible = responsibleRepository.findByTypeAndDate(type, date).orElseThrow(
                ResponsibleNotFoundException::new
        );

        //Если пользователь не найден (Не назначен), то на мобилку идет пустота
        if (responsible.getUser() == null) {
            return new UserShortResponseDto("", "", "");
        }

        UserNameWithIdResponse user = userServiceClient.getUserByIdShort(responsible.getUser());

        return new UserShortResponseDto(user.firstName(), user.lastName(), user.middleName());
    }


    private Responsible firstSetResponsible(ResponsibleSetDto responsibleSetDto) {
        //Ищем любой слот в этом дне
        if (timeSlotRepository.findEarlierStartTimeByTypeAndStartTimeOnSpecificDay(
                responsibleSetDto.type(),
                responsibleSetDto.date().atTime(0, 0),
                responsibleSetDto.date().atTime(23, 59)
        ).isPresent()) {
            //Назначаем ответственного, если нет ответственного и если вообще есть расписание (слоты) на день
            var responsible = new Responsible();
            responsible.setType(responsibleSetDto.type());
            responsible.setDate(responsibleSetDto.date());
            return responsible;
        } else {
            return null;
        }
    }

    private Boolean userHasResponsibleRole(ResponsibleSetDto responsibleSetDto) {
        //Собирается все роли человека, которого ставим
        List<String> roles = userServiceClient.getAllRolesByUserId(responsibleSetDto.user());
        for (String role : roles) {
            //Если есть у человека роль ответственного за что-то, то ответственному ставим человека,
            // которому хотим записать(Если у человека есть роль на что-то, то его только на это и можно поставаить
            // (Если поставить человека не ответственного за зал в ответственного, то нельзя так))
            if (role.contains(responsibleSetDto.type().toString())) {
                return true;
            }
        }
        return false;
    }
}

