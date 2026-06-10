package ru.tpu.hostel.schedules.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import ru.tpu.hostel.internal.exception.ServiceException;
import ru.tpu.hostel.internal.utils.ExecutionContext;
import ru.tpu.hostel.internal.utils.Roles;
import ru.tpu.hostel.internal.utils.TimeUtil;
import ru.tpu.hostel.schedules.TestData;
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
import ru.tpu.hostel.schedules.service.responsible.assigner.ResponsibleAssignChecker;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResponsibleServiceImplTest {

    @Mock
    private ResponsibleRepository responsibleRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private ResponsibleMapper responsibleMapper;

    @Mock
    private ResponsibleAssignChecker gymChecker;

    private ResponsibleServiceImpl responsibleService;

    private ResponsibleServiceImpl service() {
        return new ResponsibleServiceImpl(
                responsibleRepository,
                userServiceClient,
                responsibleMapper,
                Set.of(gymChecker)
        );
    }

    @Test
    void setResponsibleWhenAssigningSelfWithSuccess() {
        responsibleService = service();
        ResponsibleSetRequestDto dto =
                TestData.responsibleSetRequestDto(TestData.DATE_TODAY, EventType.GYM, null);
        ResponsibleResponseDto expected =
                new ResponsibleResponseDto(TestData.DATE_TODAY, EventType.GYM, TestData.USER_ID);
        when(gymChecker.isApplicable(EventType.GYM)).thenReturn(true);
        when(responsibleMapper.mapToResponsibleResponseDto(any(Responsible.class))).thenReturn(expected);

        try (MockedStatic<ExecutionContext> contextMock = mockStatic(ExecutionContext.class)) {
            ExecutionContext context = mock(ExecutionContext.class);
            contextMock.when(ExecutionContext::get).thenReturn(context);
            when(context.getUserRoles()).thenReturn(Set.of(Roles.RESPONSIBLE_GYM));
            when(context.getUserID()).thenReturn(TestData.USER_ID);

            ResponsibleResponseDto result = responsibleService.setResponsible(dto);

            assertThat(result).isEqualTo(expected);
            verify(responsibleRepository).save(any(Responsible.class));
            verify(responsibleRepository).flush();
        }
    }

    @Test
    void setResponsibleWhenAssigningOtherUserWithSuccess() {
        responsibleService = service();
        ResponsibleSetRequestDto dto =
                TestData.responsibleSetRequestDto(TestData.DATE_TODAY, EventType.GYM, TestData.OTHER_USER_ID);
        ResponsibleResponseDto expected =
                new ResponsibleResponseDto(TestData.DATE_TODAY, EventType.GYM, TestData.OTHER_USER_ID);
        when(gymChecker.isApplicable(EventType.GYM)).thenReturn(true);
        when(userServiceClient.getAllRolesByUserId(TestData.OTHER_USER_ID))
                .thenReturn(List.of(Roles.RESPONSIBLE_GYM));
        when(responsibleMapper.mapToResponsibleResponseDto(any(Responsible.class))).thenReturn(expected);

        try (MockedStatic<ExecutionContext> contextMock = mockStatic(ExecutionContext.class)) {
            ExecutionContext context = mock(ExecutionContext.class);
            contextMock.when(ExecutionContext::get).thenReturn(context);
            when(context.getUserRoles()).thenReturn(Set.of(Roles.HOSTEL_SUPERVISOR));

            ResponsibleResponseDto result = responsibleService.setResponsible(dto);

            assertThat(result).isEqualTo(expected);
            verify(responsibleRepository).save(any(Responsible.class));
        }
    }

    @Test
    void setResponsibleWhenCheckerNotFound() {
        responsibleService = service();
        ResponsibleSetRequestDto dto =
                TestData.responsibleSetRequestDto(TestData.DATE_TODAY, EventType.GYM, null);
        when(gymChecker.isApplicable(EventType.GYM)).thenReturn(false);

        assertThatThrownBy(() -> responsibleService.setResponsible(dto))
                .isInstanceOf(ServiceException.NotImplemented.class);
    }

    @Test
    void setResponsibleWhenForbidden() {
        responsibleService = service();
        ResponsibleSetRequestDto dto =
                TestData.responsibleSetRequestDto(TestData.DATE_TODAY, EventType.GYM, null);
        when(gymChecker.isApplicable(EventType.GYM)).thenReturn(true);

        try (MockedStatic<ExecutionContext> contextMock = mockStatic(ExecutionContext.class)) {
            ExecutionContext context = mock(ExecutionContext.class);
            contextMock.when(ExecutionContext::get).thenReturn(context);
            when(context.getUserRoles()).thenReturn(Set.of(Roles.STUDENT));

            assertThatThrownBy(() -> responsibleService.setResponsible(dto))
                    .isInstanceOf(ServiceException.Forbidden.class);
        }
    }

    @Test
    void setResponsibleWhenUserAlreadySigned() {
        responsibleService = service();
        ResponsibleSetRequestDto dto =
                TestData.responsibleSetRequestDto(TestData.DATE_TODAY, EventType.GYM, null);
        when(gymChecker.isApplicable(EventType.GYM)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("dup")).when(responsibleRepository).flush();

        try (MockedStatic<ExecutionContext> contextMock = mockStatic(ExecutionContext.class)) {
            ExecutionContext context = mock(ExecutionContext.class);
            contextMock.when(ExecutionContext::get).thenReturn(context);
            when(context.getUserRoles()).thenReturn(Set.of(Roles.RESPONSIBLE_GYM));
            when(context.getUserID()).thenReturn(TestData.USER_ID);

            assertThatThrownBy(() -> responsibleService.setResponsible(dto))
                    .isInstanceOf(ServiceException.Conflict.class);
        }
    }

    @Test
    void editResponsibleWhenEditingToSelfWithSuccess() {
        responsibleService = service();
        Responsible responsible = TestData.defaultResponsible();
        ResponsibleEditRequestDto dto = TestData.responsibleEditRequestDto(null);
        ResponsibleResponseDto expected =
                new ResponsibleResponseDto(TestData.DATE_TODAY, EventType.GYM, TestData.OTHER_USER_ID);
        when(responsibleRepository.findByIdOptimistic(TestData.RESPONSIBLE_ID))
                .thenReturn(Optional.of(responsible));
        when(responsibleMapper.mapToResponsibleResponseDto(responsible)).thenReturn(expected);

        try (MockedStatic<ExecutionContext> contextMock = mockStatic(ExecutionContext.class)) {
            ExecutionContext context = mock(ExecutionContext.class);
            contextMock.when(ExecutionContext::get).thenReturn(context);
            when(context.getUserRoles()).thenReturn(Set.of(Roles.RESPONSIBLE_GYM));
            when(context.getUserID()).thenReturn(TestData.OTHER_USER_ID);

            ResponsibleResponseDto result =
                    responsibleService.editResponsible(TestData.RESPONSIBLE_ID, dto);

            assertThat(result).isEqualTo(expected);
            assertThat(responsible.getUser()).isEqualTo(TestData.OTHER_USER_ID);
        }
    }

    @Test
    void editResponsibleWhenEditingToOtherUserWithSuccess() {
        responsibleService = service();
        Responsible responsible = TestData.defaultResponsible();
        ResponsibleEditRequestDto dto = TestData.responsibleEditRequestDto(TestData.OTHER_USER_ID);
        ResponsibleResponseDto expected =
                new ResponsibleResponseDto(TestData.DATE_TODAY, EventType.GYM, TestData.OTHER_USER_ID);
        when(responsibleRepository.findByIdOptimistic(TestData.RESPONSIBLE_ID))
                .thenReturn(Optional.of(responsible));
        when(userServiceClient.getAllRolesByUserId(TestData.OTHER_USER_ID))
                .thenReturn(List.of(Roles.RESPONSIBLE_GYM));
        when(responsibleMapper.mapToResponsibleResponseDto(responsible)).thenReturn(expected);

        try (MockedStatic<ExecutionContext> contextMock = mockStatic(ExecutionContext.class)) {
            ExecutionContext context = mock(ExecutionContext.class);
            contextMock.when(ExecutionContext::get).thenReturn(context);
            when(context.getUserRoles()).thenReturn(Set.of(Roles.HOSTEL_SUPERVISOR));

            ResponsibleResponseDto result =
                    responsibleService.editResponsible(TestData.RESPONSIBLE_ID, dto);

            assertThat(result).isEqualTo(expected);
        }
    }

    @Test
    void editResponsibleWhenNotFound() {
        responsibleService = service();
        ResponsibleEditRequestDto dto = TestData.responsibleEditRequestDto(null);
        when(responsibleRepository.findByIdOptimistic(TestData.RESPONSIBLE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> responsibleService.editResponsible(TestData.RESPONSIBLE_ID, dto))
                .isInstanceOf(ServiceException.NotFound.class);
    }

    @Test
    void editResponsibleWhenForbidden() {
        responsibleService = service();
        Responsible responsible = TestData.defaultResponsible();
        ResponsibleEditRequestDto dto = TestData.responsibleEditRequestDto(null);
        when(responsibleRepository.findByIdOptimistic(TestData.RESPONSIBLE_ID))
                .thenReturn(Optional.of(responsible));

        try (MockedStatic<ExecutionContext> contextMock = mockStatic(ExecutionContext.class)) {
            ExecutionContext context = mock(ExecutionContext.class);
            contextMock.when(ExecutionContext::get).thenReturn(context);
            when(context.getUserRoles()).thenReturn(Set.of(Roles.STUDENT));

            assertThatThrownBy(() -> responsibleService.editResponsible(TestData.RESPONSIBLE_ID, dto))
                    .isInstanceOf(ServiceException.Forbidden.class);
        }
    }

    @Test
    void editResponsibleWhenOptimisticLock() {
        responsibleService = service();
        Responsible responsible = TestData.defaultResponsible();
        ResponsibleEditRequestDto dto = TestData.responsibleEditRequestDto(null);
        when(responsibleRepository.findByIdOptimistic(TestData.RESPONSIBLE_ID))
                .thenReturn(Optional.of(responsible));
        doThrow(new ObjectOptimisticLockingFailureException(Responsible.class, TestData.RESPONSIBLE_ID))
                .when(responsibleRepository).flush();

        try (MockedStatic<ExecutionContext> contextMock = mockStatic(ExecutionContext.class)) {
            ExecutionContext context = mock(ExecutionContext.class);
            contextMock.when(ExecutionContext::get).thenReturn(context);
            when(context.getUserRoles()).thenReturn(Set.of(Roles.RESPONSIBLE_GYM));
            when(context.getUserID()).thenReturn(TestData.OTHER_USER_ID);

            assertThatThrownBy(() -> responsibleService.editResponsible(TestData.RESPONSIBLE_ID, dto))
                    .isInstanceOf(ServiceException.Conflict.class);
        }
    }

    @Test
    void editResponsibleWhenDataIntegrityViolation() {
        responsibleService = service();
        Responsible responsible = TestData.defaultResponsible();
        ResponsibleEditRequestDto dto = TestData.responsibleEditRequestDto(null);
        when(responsibleRepository.findByIdOptimistic(TestData.RESPONSIBLE_ID))
                .thenReturn(Optional.of(responsible));
        doThrow(new DataIntegrityViolationException("dup")).when(responsibleRepository).flush();

        try (MockedStatic<ExecutionContext> contextMock = mockStatic(ExecutionContext.class)) {
            ExecutionContext context = mock(ExecutionContext.class);
            contextMock.when(ExecutionContext::get).thenReturn(context);
            when(context.getUserRoles()).thenReturn(Set.of(Roles.RESPONSIBLE_GYM));
            when(context.getUserID()).thenReturn(TestData.OTHER_USER_ID);

            assertThatThrownBy(() -> responsibleService.editResponsible(TestData.RESPONSIBLE_ID, dto))
                    .isInstanceOf(ServiceException.Conflict.class);
        }
    }

    @Test
    void getResponsibleByTypeAndDateWhenFound() {
        responsibleService = service();
        Responsible responsible = TestData.defaultResponsible();
        when(responsibleRepository.findByTypeAndDate(EventType.GYM, TestData.DATE_TODAY))
                .thenReturn(Optional.of(responsible));
        when(userServiceClient.getUserByIdShort(TestData.USER_ID))
                .thenReturn(TestData.userNameWithIdResponse(TestData.USER_ID));

        UserShortResponseDto result =
                responsibleService.getResponsibleByTypeAndDate(TestData.DATE_TODAY, EventType.GYM);

        assertThat(result.firstName()).isEqualTo(TestData.FIRST_NAME_IVAN);
        assertThat(result.tgLink()).isEqualTo(TestData.TG_LINK);
    }

    @Test
    void getResponsibleByTypeAndDateWhenNotFound() {
        responsibleService = service();
        when(responsibleRepository.findByTypeAndDate(EventType.GYM, TestData.DATE_TODAY))
                .thenReturn(Optional.empty());

        UserShortResponseDto result =
                responsibleService.getResponsibleByTypeAndDate(TestData.DATE_TODAY, EventType.GYM);

        assertThat(result.firstName()).isEmpty();
    }

    @Test
    void getAllResponsibleByTypeAndDateWhenFound() {
        responsibleService = service();
        Responsible responsible = TestData.defaultResponsible();
        UserNameWithIdResponse user = TestData.userNameWithIdResponse(TestData.USER_ID);
        when(responsibleRepository.findAllByTypeAndDate(EventType.GYM, TestData.DATE_TODAY))
                .thenReturn(List.of(responsible));
        when(userServiceClient.getAllUsersWithIdsShort(any(java.util.UUID[].class)))
                .thenReturn(List.of(user));
        when(responsibleMapper.mapToUserNameWithIdResponse(TestData.USER_ID, user)).thenReturn(user);

        List<UserNameWithIdResponse> result =
                responsibleService.getAllResponsibleByTypeAndDate(TestData.DATE_TODAY, EventType.GYM);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllResponsibleByTypeAndDateWhenEmpty() {
        responsibleService = service();
        when(responsibleRepository.findAllByTypeAndDate(EventType.GYM, TestData.DATE_TODAY))
                .thenReturn(List.of());

        List<UserNameWithIdResponse> result =
                responsibleService.getAllResponsibleByTypeAndDate(TestData.DATE_TODAY, EventType.GYM);

        assertThat(result).isEmpty();
    }

    @Test
    void getAllResponsibleByTypeAndDateWhenUserNotInMap() {
        responsibleService = service();
        Responsible responsible = TestData.defaultResponsible();
        UserNameWithIdResponse other = TestData.userNameWithIdResponse(TestData.OTHER_USER_ID);
        when(responsibleRepository.findAllByTypeAndDate(EventType.GYM, TestData.DATE_TODAY))
                .thenReturn(List.of(responsible));
        when(userServiceClient.getAllUsersWithIdsShort(any(java.util.UUID[].class)))
                .thenReturn(List.of(other));
        when(responsibleMapper.mapToUserNameWithIdResponse(any(), any())).thenReturn(other);

        List<UserNameWithIdResponse> result =
                responsibleService.getAllResponsibleByTypeAndDate(TestData.DATE_TODAY, EventType.GYM);

        assertThat(result).hasSize(1);
    }

    @Test
    void getActiveResponsibleWithSuccess() {
        responsibleService = service();
        Responsible responsible = TestData.defaultResponsible();
        ActiveEventResponseDto active = new ActiveEventResponseDto(
                TestData.RESPONSIBLE_ID, TestData.NOW, TestData.NOW, "BOOKED", "type");
        try (MockedStatic<ExecutionContext> contextMock = mockStatic(ExecutionContext.class);
             MockedStatic<TimeUtil> timeMock = mockStatic(TimeUtil.class)) {
            ExecutionContext context = mock(ExecutionContext.class);
            contextMock.when(ExecutionContext::get).thenReturn(context);
            when(context.getUserID()).thenReturn(TestData.USER_ID);
            timeMock.when(TimeUtil::now).thenReturn(TestData.NOW);
            when(responsibleRepository.findAllActiveResponsible(any(), any(), any()))
                    .thenReturn(List.of(responsible));
            when(responsibleMapper.mapToActiveEventResponseDto(responsible)).thenReturn(active);

            List<ActiveEventResponseDto> result = responsibleService.getActiveResponsible();

            assertThat(result).containsExactly(active);
        }
    }

    @Test
    void deleteResponsibleWithSuccess() {
        responsibleService = service();
        Responsible responsible = TestData.defaultResponsible();
        when(responsibleRepository.findByUserTypeDateOptimistic(
                TestData.USER_ID, EventType.GYM, TestData.DATE_TODAY))
                .thenReturn(Optional.of(responsible));

        try (MockedStatic<ExecutionContext> contextMock = mockStatic(ExecutionContext.class)) {
            ExecutionContext context = mock(ExecutionContext.class);
            contextMock.when(ExecutionContext::get).thenReturn(context);
            when(context.getUserRoles()).thenReturn(Set.of(Roles.RESPONSIBLE_GYM));

            responsibleService.deleteResponsible(TestData.USER_ID, EventType.GYM, TestData.DATE_TODAY);

            verify(responsibleRepository).delete(responsible);
            verify(responsibleRepository).flush();
        }
    }

    @Test
    void deleteResponsibleWhenNotFound() {
        responsibleService = service();
        when(responsibleRepository.findByUserTypeDateOptimistic(
                TestData.USER_ID, EventType.GYM, TestData.DATE_TODAY))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                responsibleService.deleteResponsible(TestData.USER_ID, EventType.GYM, TestData.DATE_TODAY))
                .isInstanceOf(ServiceException.NotFound.class);
    }

    @Test
    void deleteResponsibleWhenForbidden() {
        responsibleService = service();
        Responsible responsible = TestData.defaultResponsible();
        when(responsibleRepository.findByUserTypeDateOptimistic(
                TestData.USER_ID, EventType.GYM, TestData.DATE_TODAY))
                .thenReturn(Optional.of(responsible));

        try (MockedStatic<ExecutionContext> contextMock = mockStatic(ExecutionContext.class)) {
            ExecutionContext context = mock(ExecutionContext.class);
            contextMock.when(ExecutionContext::get).thenReturn(context);
            when(context.getUserRoles()).thenReturn(Set.of(Roles.STUDENT));

            assertThatThrownBy(() ->
                    responsibleService.deleteResponsible(TestData.USER_ID, EventType.GYM, TestData.DATE_TODAY))
                    .isInstanceOf(ServiceException.Forbidden.class);
        }
    }
}
