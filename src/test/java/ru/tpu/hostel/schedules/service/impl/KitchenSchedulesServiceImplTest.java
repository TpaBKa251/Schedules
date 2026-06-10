package ru.tpu.hostel.schedules.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tpu.hostel.internal.utils.ExecutionContext;
import ru.tpu.hostel.internal.utils.TimeUtil;
import ru.tpu.hostel.schedules.TestData;
import ru.tpu.hostel.schedules.dto.response.ActiveEventResponseDto;
import ru.tpu.hostel.schedules.dto.response.KitchenScheduleResponseDto;
import ru.tpu.hostel.schedules.entity.KitchenSchedule;
import ru.tpu.hostel.schedules.external.rest.user.UserServiceClient;
import ru.tpu.hostel.schedules.repository.KitchenSchedulesRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.tpu.hostel.internal.exception.ServiceException.Conflict;
import static ru.tpu.hostel.internal.exception.ServiceException.Forbidden;
import static ru.tpu.hostel.internal.exception.ServiceException.NotFound;

@ExtendWith(MockitoExtension.class)
class KitchenSchedulesServiceImplTest {

    @Mock
    private KitchenSchedulesRepository kitchenSchedulesRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private KitchenSchedulesServiceImpl kitchenSchedulesService;

    @Test
    void getScheduleWithFloor() {
        try (MockedStatic<TimeUtil> timeUtilMock = mockStatic(TimeUtil.class)) {
            timeUtilMock.when(TimeUtil::now).thenReturn(TestData.NOW);
            when(kitchenSchedulesRepository.findAllOnFloorAfterDate(eq(TestData.FLOOR_2), any(LocalDate.class)))
                    .thenReturn(List.of(TestData.defaultKitchenSchedule()));
            when(userServiceClient.getAllInRooms(any(String[].class))).thenReturn(List.of(TestData.userResponseDto()));

            List<KitchenScheduleResponseDto> result = kitchenSchedulesService.getSchedule(TestData.FLOOR_2);

            assertThat(result).hasSize(1);
        }
    }

    @Test
    void getActiveDutiesWithRoom() {
        try (MockedStatic<TimeUtil> timeUtilMock = mockStatic(TimeUtil.class)) {
            timeUtilMock.when(TimeUtil::now).thenReturn(TestData.NOW);
            when(kitchenSchedulesRepository.findAllActiveDuties(eq(TestData.ROOM_NUMBER_201), any(), any()))
                    .thenReturn(List.of(TestData.defaultKitchenSchedule()));

            List<ActiveEventResponseDto> result = kitchenSchedulesService.getActiveDuties(TestData.ROOM_NUMBER_201);

            assertThat(result).hasSize(1);
        }
    }

    @Test
    void getDutyByIdWhenFound() {
        when(kitchenSchedulesRepository.findById(TestData.KITCHEN_ID))
                .thenReturn(Optional.of(TestData.defaultKitchenSchedule()));
        when(userServiceClient.getAllInRooms(any(String[].class))).thenReturn(List.of(TestData.userResponseDto()));

        KitchenScheduleResponseDto result = kitchenSchedulesService.getDutyById(TestData.KITCHEN_ID);

        assertThat(result).isNotNull();
    }

    @Test
    void getDutyByIdWhenNotFound() {
        when(kitchenSchedulesRepository.findById(TestData.KITCHEN_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kitchenSchedulesService.getDutyById(TestData.KITCHEN_ID))
                .isInstanceOf(NotFound.class);
    }

    @Test
    void swapDutiesWithSuccess() {
        KitchenSchedule scheduleA = TestData.newKitchenSchedule(
                TestData.KITCHEN_ID, TestData.DATE_TODAY, TestData.ROOM_NUMBER_201, false);
        KitchenSchedule scheduleB = TestData.newKitchenSchedule(
                TestData.OTHER_KITCHEN_ID, TestData.DATE_TODAY, TestData.ROOM_NUMBER_202, false);
        when(kitchenSchedulesRepository.findDutiesForSwap(TestData.KITCHEN_ID, TestData.OTHER_KITCHEN_ID))
                .thenReturn(List.of(scheduleA, scheduleB));

        try (MockedStatic<TimeUtil> timeUtilMock = mockStatic(TimeUtil.class);
             MockedStatic<ExecutionContext> contextMock = mockStatic(ExecutionContext.class)) {
            timeUtilMock.when(TimeUtil::now).thenReturn(TestData.NOW);
            ExecutionContext context = mock(ExecutionContext.class);
            contextMock.when(ExecutionContext::get).thenReturn(context);
            when(context.getUserID()).thenReturn(TestData.USER_ID);
            when(userServiceClient.getRoomNumber(TestData.USER_ID)).thenReturn(TestData.ROOM_NUMBER_201);

            kitchenSchedulesService.swapDuties(TestData.swapRequestDto(TestData.KITCHEN_ID, TestData.OTHER_KITCHEN_ID));

            assertThat(scheduleA.getRoomNumber()).isEqualTo(TestData.ROOM_NUMBER_202);
            verify(kitchenSchedulesRepository).flush();
        }
    }

    @Test
    void swapDutiesWhenNotFound() {
        when(kitchenSchedulesRepository.findDutiesForSwap(TestData.KITCHEN_ID, TestData.OTHER_KITCHEN_ID))
                .thenReturn(List.of(TestData.defaultKitchenSchedule()));

        assertThatThrownBy(() -> kitchenSchedulesService.swapDuties(
                TestData.swapRequestDto(TestData.KITCHEN_ID, TestData.OTHER_KITCHEN_ID)))
                .isInstanceOf(NotFound.class);
    }

    @Test
    void swapDutiesWhenForbidden() {
        KitchenSchedule scheduleA = TestData.newKitchenSchedule(
                TestData.KITCHEN_ID, TestData.DATE_TODAY, TestData.ROOM_NUMBER_201, false);
        KitchenSchedule scheduleB = TestData.newKitchenSchedule(
                TestData.OTHER_KITCHEN_ID, TestData.DATE_TODAY, TestData.ROOM_NUMBER_202, false);
        when(kitchenSchedulesRepository.findDutiesForSwap(TestData.KITCHEN_ID, TestData.OTHER_KITCHEN_ID))
                .thenReturn(List.of(scheduleA, scheduleB));

        try (MockedStatic<TimeUtil> timeUtilMock = mockStatic(TimeUtil.class);
             MockedStatic<ExecutionContext> contextMock = mockStatic(ExecutionContext.class)) {
            timeUtilMock.when(TimeUtil::now).thenReturn(TestData.NOW);
            ExecutionContext context = mock(ExecutionContext.class);
            contextMock.when(ExecutionContext::get).thenReturn(context);
            when(context.getUserID()).thenReturn(TestData.USER_ID);
            when(userServiceClient.getRoomNumber(TestData.USER_ID)).thenReturn(TestData.ROOM_NUMBER_301);

            assertThatThrownBy(() -> kitchenSchedulesService.swapDuties(
                    TestData.swapRequestDto(TestData.KITCHEN_ID, TestData.OTHER_KITCHEN_ID)))
                    .isInstanceOf(Forbidden.class);
        }
    }

    @Test
    void markDutyWithSuccess() {
        KitchenSchedule schedule = TestData.newKitchenSchedule(
                TestData.KITCHEN_ID, TestData.DATE_TODAY, TestData.ROOM_NUMBER_201, false);
        when(kitchenSchedulesRepository.findByIdForUpdateOptimistic(TestData.KITCHEN_ID))
                .thenReturn(Optional.of(schedule));

        try (MockedStatic<TimeUtil> timeUtilMock = mockStatic(TimeUtil.class);
             MockedStatic<ExecutionContext> contextMock = mockStatic(ExecutionContext.class)) {
            timeUtilMock.when(TimeUtil::now).thenReturn(TestData.NOW);
            ExecutionContext context = mock(ExecutionContext.class);
            contextMock.when(ExecutionContext::get).thenReturn(context);
            when(context.getUserID()).thenReturn(TestData.USER_ID);
            when(userServiceClient.getRoomNumber(TestData.USER_ID)).thenReturn(TestData.ROOM_NUMBER_201);

            kitchenSchedulesService.markDuty(TestData.KITCHEN_ID);

            assertThat(schedule.getChecked()).isTrue();
            verify(kitchenSchedulesRepository).flush();
        }
    }

    @Test
    void markDutyWhenNotFound() {
        when(kitchenSchedulesRepository.findByIdForUpdateOptimistic(TestData.KITCHEN_ID))
                .thenReturn(Optional.empty());

        try (MockedStatic<TimeUtil> timeUtilMock = mockStatic(TimeUtil.class)) {
            timeUtilMock.when(TimeUtil::now).thenReturn(TestData.NOW);

            assertThatThrownBy(() -> kitchenSchedulesService.markDuty(TestData.KITCHEN_ID))
                    .isInstanceOf(NotFound.class);
        }
    }

    @Test
    void deleteDutyByIdWithSuccess() {
        KitchenSchedule schedule = TestData.newKitchenSchedule(
                TestData.KITCHEN_ID, TestData.DATE_TODAY, TestData.ROOM_NUMBER_201, false);
        when(kitchenSchedulesRepository.findByIdForUpdateOptimistic(TestData.KITCHEN_ID))
                .thenReturn(Optional.of(schedule));
        when(kitchenSchedulesRepository.findAllByFloorFromDate(anyString(), any(LocalDate.class)))
                .thenReturn(List.of());

        try (MockedStatic<TimeUtil> timeUtilMock = mockStatic(TimeUtil.class);
             MockedStatic<ExecutionContext> contextMock = mockStatic(ExecutionContext.class)) {
            timeUtilMock.when(TimeUtil::now).thenReturn(TestData.NOW);
            ExecutionContext context = mock(ExecutionContext.class);
            contextMock.when(ExecutionContext::get).thenReturn(context);
            when(context.getUserID()).thenReturn(TestData.USER_ID);
            when(userServiceClient.getRoomNumber(TestData.USER_ID)).thenReturn(TestData.ROOM_NUMBER_201);

            kitchenSchedulesService.deleteDutyById(TestData.KITCHEN_ID);

            verify(kitchenSchedulesRepository).delete(schedule);
        }
    }

    @Test
    void deleteDutyByIdWhenNotFound() {
        when(kitchenSchedulesRepository.findByIdForUpdateOptimistic(TestData.KITCHEN_ID))
                .thenReturn(Optional.empty());

        try (MockedStatic<TimeUtil> timeUtilMock = mockStatic(TimeUtil.class)) {
            timeUtilMock.when(TimeUtil::now).thenReturn(TestData.NOW);

            assertThatThrownBy(() -> kitchenSchedulesService.deleteDutyById(TestData.KITCHEN_ID))
                    .isInstanceOf(NotFound.class);
        }
    }
}
