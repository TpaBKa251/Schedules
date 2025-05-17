package ru.tpu.hostel.schedules.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.tpu.hostel.internal.exception.ServiceException;
import ru.tpu.hostel.internal.utils.ExecutionContext;
import ru.tpu.hostel.internal.utils.Roles;
import ru.tpu.hostel.schedules.dto.request.ResponsibleEditRequestDto;
import ru.tpu.hostel.schedules.dto.request.ResponsibleSetRequestDto;
import ru.tpu.hostel.schedules.dto.response.ResponsibleResponseDto;
import ru.tpu.hostel.schedules.dto.response.UserNameWithIdResponse;
import ru.tpu.hostel.schedules.dto.response.UserShortResponseDto;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.entity.Responsible;
import ru.tpu.hostel.schedules.external.rest.user.UserServiceClient;
import ru.tpu.hostel.schedules.mapper.ResponsibleMapper;
import ru.tpu.hostel.schedules.repository.ResponsibleRepository;
import ru.tpu.hostel.schedules.service.ResponsibleService;
import ru.tpu.hostel.schedules.service.responsible.assigner.ResponsibleAssignChecker;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResponsibleServiceImpl implements ResponsibleService {

    private static final String CONFLICT_VERSIONS_EXCEPTION_MESSAGE
            = "Кто-то уже изменил ответственного. Обновите данные и повторите попытку";

    private static final String RESPONSIBLE_NOT_FOUND_EXCEPTION_MESSAGE = "Ответственный не найден";

    private final ResponsibleRepository responsibleRepository;

    private final UserServiceClient userServiceClient;

    private final ResponsibleMapper responsibleMapper;

    private final Set<ResponsibleAssignChecker> responsibleAssignCheckers;

    @Transactional
    @Override
    public ResponsibleResponseDto setResponsible(ResponsibleSetRequestDto responsibleSetRequestDto) {
        responsibleAssignCheckers.stream()
                .filter(checker -> checker.isApplicable(responsibleSetRequestDto.type()))
                .findFirst()
                .ifPresentOrElse(
                        checker -> checker.canAssignResponsible(responsibleSetRequestDto),
                        () -> {
                            throw new ServiceException.NotImplemented(
                                    "Невозможно обработать тип " + responsibleSetRequestDto.type().getEventTypeName()
                            );
                        }
                );

        ExecutionContext context = ExecutionContext.get();
        if (responsibleSetRequestDto.user() == null
                && Roles.canBeAssignedToResourceType(context.getUserRoles(), responsibleSetRequestDto.type())) {
            return setResponsible(
                    context.getUserID(),
                    responsibleSetRequestDto.type(),
                    responsibleSetRequestDto.date()
            );
        }

        if (responsibleSetRequestDto.user() != null) {
            List<Roles> userToAssignRoles = userServiceClient.getAllRolesByUserId(responsibleSetRequestDto.user());
            if (Roles.isRoleHigherThan(context.getUserRoles(), userToAssignRoles)
                    && Roles.hasPermissionToManageResourceType(context.getUserRoles(), responsibleSetRequestDto.type())
                    && Roles.canBeAssignedToResourceType(userToAssignRoles, responsibleSetRequestDto.type())) {
                return setResponsible(
                        responsibleSetRequestDto.user(),
                        responsibleSetRequestDto.type(),
                        responsibleSetRequestDto.date()
                );
            }
        }

        throw new ServiceException.Forbidden("Вы не можете назначить ответственного на день");
    }

    private ResponsibleResponseDto setResponsible(UUID userId, EventType eventType, LocalDate date) {
        Responsible responsible = new Responsible();
        responsible.setDate(date);
        responsible.setType(eventType);
        responsible.setUser(userId);
        return responsibleMapper.mapToResponsibleResponseDto(responsibleRepository.save(responsible));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Override
    public ResponsibleResponseDto editResponsible(
            UUID responsibleId,
            ResponsibleEditRequestDto responsibleEditRequestDto
    ) {
        Responsible responsible = responsibleRepository.findByIdOptimistic(responsibleId).orElseThrow(
                () -> new ServiceException.NotFound(RESPONSIBLE_NOT_FOUND_EXCEPTION_MESSAGE)
        );
        ExecutionContext context = ExecutionContext.get();
        try {
            if (responsibleEditRequestDto.user() == null
                    && Roles.hasPermissionToManageResourceType(context.getUserRoles(), responsible.getType())
                    && Roles.canBeAssignedToResourceType(context.getUserRoles(), responsible.getType())) {
                responsible.setUser(context.getUserID());
                return responsibleMapper.mapToResponsibleResponseDto(responsibleRepository.save(responsible));
            }

            List<Roles> userToAssignRoles = userServiceClient.getAllRolesByUserId(responsibleEditRequestDto.user());
            if (Roles.isRoleHigherThan(context.getUserRoles(), userToAssignRoles)
                    && Roles.hasPermissionToManageResourceType(context.getUserRoles(), responsible.getType())
                    && Roles.canBeAssignedToResourceType(userToAssignRoles, responsible.getType())) {
                responsible.setUser(responsibleEditRequestDto.user());
                return responsibleMapper.mapToResponsibleResponseDto(responsibleRepository.save(responsible));
            }
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ServiceException.Conflict(CONFLICT_VERSIONS_EXCEPTION_MESSAGE);
        }

        throw new ServiceException.Forbidden("Вы не можете редактировать ответственного на день");
    }

    @Override
    public UserShortResponseDto getResponsibleByTypeAndDate(LocalDate date, EventType type) {
        Responsible responsible = responsibleRepository.findByTypeAndDate(type, date).orElse(null);

        if (responsible == null || responsible.getUser() == null) {
            return new UserShortResponseDto("", "", "");
        }

        UserNameWithIdResponse user = userServiceClient.getUserByIdShort(responsible.getUser());
        return new UserShortResponseDto(user.firstName(), user.lastName(), user.middleName());
    }

    @Override
    public List<UserNameWithIdResponse> getAllResponsibleByTypeAndDate(LocalDate date, EventType type) {
        return responsibleRepository.findAllByTypeAndDate(type, date).stream()
                .filter(r -> r.getUser() != null)
                .map(r -> responsibleMapper.mapToUserNameWithIdResponse(
                        r.getId(),
                        userServiceClient.getUserByIdShort(r.getUser())
                ))
                .toList();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Override
    public void deleteResponsible(UUID responsibleId) {
        Responsible responsible = responsibleRepository.findByIdOptimistic(responsibleId).orElseThrow(
                () -> new ServiceException.NotFound(RESPONSIBLE_NOT_FOUND_EXCEPTION_MESSAGE)
        );
        ExecutionContext context = ExecutionContext.get();

        try {
            if (Roles.hasPermissionToManageResourceType(context.getUserRoles(), responsible.getType())) {
                responsibleRepository.delete(responsible);
            } else {
                throw new ServiceException.Forbidden("Вы не можете удалить ответственного на день");
            }
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ServiceException.Conflict(CONFLICT_VERSIONS_EXCEPTION_MESSAGE);
        }
    }

}

