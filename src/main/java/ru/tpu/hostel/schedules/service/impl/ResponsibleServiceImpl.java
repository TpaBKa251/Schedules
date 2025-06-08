package ru.tpu.hostel.schedules.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tpu.hostel.internal.exception.ServiceException;
import ru.tpu.hostel.internal.utils.ExecutionContext;
import ru.tpu.hostel.internal.utils.Roles;
import ru.tpu.hostel.internal.utils.TimeUtil;
import ru.tpu.hostel.schedules.dto.request.ResponsibleEditRequestDto;
import ru.tpu.hostel.schedules.dto.request.ResponsibleSetRequestDto;
import ru.tpu.hostel.schedules.dto.response.ActiveEventResponseDto;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

// TODO Добавить кэширование userRoles (ролей юзера, которого назначают оветственным)
@Slf4j
@Service
@RequiredArgsConstructor
public class ResponsibleServiceImpl implements ResponsibleService {

    private static final String CONFLICT_VERSIONS_EXCEPTION_MESSAGE
            = "Кто-то уже изменил ответственного. Обновите данные и повторите попытку";

    private static final String RESPONSIBLE_NOT_FOUND_EXCEPTION_MESSAGE = "Ответственный не найден";

    private static final String CONFLICT_USER_ALREADY_SIGNED_EXCEPTION_MESSAGE
            = "Пользователь уже записан ответственным";

    private static final String FORBIDDEN_EXCEPTION_MESSAGE = "Вы не можете управлять ответственными за ";

    private static final UserShortResponseDto EMPTY_USER_SHORT_RESPONSE_DTO
            = new UserShortResponseDto("", "", "");

    private static final UserNameWithIdResponse EMPTY_USER_NAME_WITH_ID_RESPONSE_DTO
            = new UserNameWithIdResponse(null, "", "", "");

    private final ResponsibleRepository responsibleRepository;

    private final UserServiceClient userServiceClient;

    private final ResponsibleMapper responsibleMapper;

    private final Set<ResponsibleAssignChecker> responsibleAssignCheckers;

    @Transactional
    @Override
    public ResponsibleResponseDto setResponsible(ResponsibleSetRequestDto responsibleSetRequestDto) {
        EventType type = responsibleSetRequestDto.type();
        responsibleAssignCheckers.stream()
                .filter(checker -> checker.isApplicable(type))
                .findFirst()
                .ifPresentOrElse(
                        checker -> checker.canAssignResponsible(responsibleSetRequestDto),
                        () -> {
                            throw new ServiceException.NotImplemented(
                                    "Невозможно обработать тип " + type.getEventTypeName()
                            );
                        }
                );

        ExecutionContext context = ExecutionContext.get();
        if (responsibleSetRequestDto.user() == null
                && Roles.canBeAssignedToResourceType(context.getUserRoles(), type)) {
            return setResponsible(
                    context.getUserID(),
                    type,
                    responsibleSetRequestDto.date()
            );
        }

        if (responsibleSetRequestDto.user() != null) {
            // TODO gRPC
            List<Roles> userToAssignRoles = userServiceClient.getAllRolesByUserId(responsibleSetRequestDto.user());
            if (Roles.isRoleHigherThan(context.getUserRoles(), userToAssignRoles)
                    && Roles.hasPermissionToManageResourceType(context.getUserRoles(), type)
                    && Roles.canBeAssignedToResourceType(userToAssignRoles, type)) {
                return setResponsible(
                        responsibleSetRequestDto.user(),
                        type,
                        responsibleSetRequestDto.date()
                );
            }
        }

        throw new ServiceException.Forbidden(FORBIDDEN_EXCEPTION_MESSAGE + type.getEventTypeName());
    }

    private ResponsibleResponseDto setResponsible(UUID userId, EventType eventType, LocalDate date) {
        Responsible responsible = new Responsible();
        responsible.setDate(date);
        responsible.setType(eventType);
        responsible.setUser(userId);
        try {
            responsibleRepository.save(responsible);
            responsibleRepository.flush();
            return responsibleMapper.mapToResponsibleResponseDto(responsible);
        } catch (DataIntegrityViolationException e) {
            throw new ServiceException.Conflict(CONFLICT_USER_ALREADY_SIGNED_EXCEPTION_MESSAGE);
        }

    }

    @Transactional
    @Override
    public ResponsibleResponseDto editResponsible(
            UUID responsibleId,
            ResponsibleEditRequestDto responsibleEditRequestDto
    ) {
        Responsible responsible = responsibleRepository.findByIdOptimistic(responsibleId)
                .orElseThrow(() -> new ServiceException.NotFound(RESPONSIBLE_NOT_FOUND_EXCEPTION_MESSAGE));

        ExecutionContext context = ExecutionContext.get();
        if (responsibleEditRequestDto.user() == null
                && Roles.hasPermissionToManageResourceType(context.getUserRoles(), responsible.getType())
                && Roles.canBeAssignedToResourceType(context.getUserRoles(), responsible.getType())) {
            return editResponsible(responsible, context.getUserID());
        }

        if (responsibleEditRequestDto.user() != null) {
            // TODO gRPC
            List<Roles> userToAssignRoles = userServiceClient.getAllRolesByUserId(responsibleEditRequestDto.user());
            if (Roles.isRoleHigherThan(context.getUserRoles(), userToAssignRoles)
                    && Roles.hasPermissionToManageResourceType(context.getUserRoles(), responsible.getType())
                    && Roles.canBeAssignedToResourceType(userToAssignRoles, responsible.getType())) {
                return editResponsible(responsible, responsibleEditRequestDto.user());
            }
        }

        throw new ServiceException.Forbidden(FORBIDDEN_EXCEPTION_MESSAGE + responsible.getType().getEventTypeName());
    }

    private ResponsibleResponseDto editResponsible(Responsible responsible, UUID newUserId) {
        responsible.setUser(newUserId);
        try {
            responsibleRepository.flush();
            return responsibleMapper.mapToResponsibleResponseDto(responsible);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ServiceException.Conflict(CONFLICT_VERSIONS_EXCEPTION_MESSAGE);
        } catch (DataIntegrityViolationException e) {
            throw new ServiceException.Conflict(CONFLICT_USER_ALREADY_SIGNED_EXCEPTION_MESSAGE);
        }
    }

    @Override
    public UserShortResponseDto getResponsibleByTypeAndDate(LocalDate date, EventType type) {
        return responsibleRepository.findByTypeAndDate(type, date)
                .map(Responsible::getUser)
                .map(userId -> {
                    // TODO gRPC
                    UserNameWithIdResponse user = userServiceClient.getUserByIdShort(userId);
                    return new UserShortResponseDto(user.firstName(), user.lastName(), user.middleName());
                })
                .orElse(EMPTY_USER_SHORT_RESPONSE_DTO);
    }

    @Override
    public List<UserNameWithIdResponse> getAllResponsibleByTypeAndDate(LocalDate date, EventType type) {
        List<Responsible> responsibles = responsibleRepository.findAllByTypeAndDate(type, date).stream()
                .filter(responsible -> responsible.getUser() != null)
                .toList();

        if (responsibles.isEmpty()) {
            return Collections.emptyList();
        }

        UUID[] userIdsForRequest = responsibles.stream()
                .map(Responsible::getUser)
                .toArray(UUID[]::new);

        // TODO gRPC
        List<UserNameWithIdResponse> users = userServiceClient.getAllUsersWithIdsShort(userIdsForRequest);
        Map<UUID, UserNameWithIdResponse> userMap = users.stream()
                .collect(Collectors.toMap(UserNameWithIdResponse::id, Function.identity()));

        return responsibles.stream()
                .map(responsible -> {
                    UserNameWithIdResponse user = userMap.get(responsible.getUser()) == null
                            ? EMPTY_USER_NAME_WITH_ID_RESPONSE_DTO
                            : userMap.get(responsible.getUser());
                    return responsibleMapper.mapToUserNameWithIdResponse(responsible.getId(), user);
                })
                .toList();
    }

    @Override
    public List<ActiveEventResponseDto> getActiveResponsible() {
        UUID userId = ExecutionContext.get().getUserID();
        LocalDate today = TimeUtil.now().toLocalDate();
        LocalDate tomorrow = today.plusDays(1);
        return responsibleRepository.findAllActiveResponsible(userId, tomorrow, today).stream()
                .map(responsibleMapper::mapToActiveEventResponseDto)
                .toList();
    }

    @Transactional
    @Override
    public void deleteResponsible(UUID responsibleId) {
        Responsible responsible = responsibleRepository.findByIdOptimistic(responsibleId)
                .orElseThrow(() -> new ServiceException.NotFound(RESPONSIBLE_NOT_FOUND_EXCEPTION_MESSAGE));

        ExecutionContext context = ExecutionContext.get();
        try {
            if (Roles.hasPermissionToManageResourceType(context.getUserRoles(), responsible.getType())) {
                responsibleRepository.delete(responsible);
                responsibleRepository.flush();
            } else {
                throw new ServiceException.Forbidden(FORBIDDEN_EXCEPTION_MESSAGE
                        + responsible.getType().getEventTypeName());
            }
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ServiceException.Conflict(CONFLICT_VERSIONS_EXCEPTION_MESSAGE);
        }
    }

}

