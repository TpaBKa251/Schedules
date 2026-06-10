package ru.tpu.hostel.schedules.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.tpu.hostel.internal.exception.ServiceException;
import ru.tpu.hostel.internal.utils.ExecutionContext;
import ru.tpu.hostel.internal.utils.TimeUtil;
import ru.tpu.hostel.schedules.TestData;
import ru.tpu.hostel.schedules.dto.response.TimeslotResponse;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.entity.Timeslot;
import ru.tpu.hostel.schedules.external.rest.booking.BookingClient;
import ru.tpu.hostel.schedules.repository.TimeslotRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeslotServiceImplTest {

    @Mock
    private TimeslotRepository repository;

    @Mock
    private BookingClient bookingClient;

    @InjectMocks
    private TimeslotServiceImpl timeslotService;

    @Test
    void getTimeslotForBookWithSuccess() {
        Timeslot timeslot = TestData.defaultTimeslot();
        try (MockedStatic<TimeUtil> timeMock = mockStatic(TimeUtil.class)) {
            timeMock.when(TimeUtil::now).thenReturn(TestData.NOW);
            when(repository.findAvailableSlotForUpdate(eq(TestData.TIMESLOT_ID), any()))
                    .thenReturn(Optional.of(timeslot));

            Timeslot result = timeslotService.getTimeslotForBook(TestData.TIMESLOT_ID);

            assertThat(result.getBookingCount()).isEqualTo(TestData.BOOKING_COUNT_1);
            verify(repository).flush();
        }
    }

    @Test
    void getTimeslotForBookWhenSlotNotAvailable() {
        try (MockedStatic<TimeUtil> timeMock = mockStatic(TimeUtil.class)) {
            timeMock.when(TimeUtil::now).thenReturn(TestData.NOW);
            when(repository.findAvailableSlotForUpdate(eq(TestData.TIMESLOT_ID), any()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> timeslotService.getTimeslotForBook(TestData.TIMESLOT_ID))
                    .isInstanceOf(ServiceException.Conflict.class);
        }
    }

    @Test
    void getTimeslotForBookWhenDataIntegrityViolation() {
        Timeslot timeslot = TestData.defaultTimeslot();
        try (MockedStatic<TimeUtil> timeMock = mockStatic(TimeUtil.class)) {
            timeMock.when(TimeUtil::now).thenReturn(TestData.NOW);
            when(repository.findAvailableSlotForUpdate(eq(TestData.TIMESLOT_ID), any()))
                    .thenReturn(Optional.of(timeslot));
            doThrow(new DataIntegrityViolationException("full")).when(repository).flush();

            assertThatThrownBy(() -> timeslotService.getTimeslotForBook(TestData.TIMESLOT_ID))
                    .isInstanceOf(ServiceException.Conflict.class);
        }
    }

    @Test
    void cancelTimeSlotWithSuccess() {
        Timeslot timeslot = TestData.newTimeslot(
                TestData.TIMESLOT_ID, TestData.SLOT_START, TestData.SLOT_END, EventType.GYM,
                TestData.LIMIT_8, TestData.BOOKING_COUNT_1);
        when(repository.findSlotForUpdate(TestData.TIMESLOT_ID)).thenReturn(Optional.of(timeslot));

        timeslotService.cancelTimeSlot(TestData.TIMESLOT_ID);

        assertThat(timeslot.getBookingCount()).isEqualTo(TestData.BOOKING_COUNT_0);
        verify(repository).flush();
    }

    @Test
    void cancelTimeSlotWhenSlotNotFound() {
        when(repository.findSlotForUpdate(TestData.TIMESLOT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> timeslotService.cancelTimeSlot(TestData.TIMESLOT_ID))
                .isInstanceOf(ServiceException.Conflict.class);
    }

    @Test
    void cancelTimeSlotWhenDataIntegrityViolation() {
        Timeslot timeslot = TestData.newTimeslot(
                TestData.TIMESLOT_ID, TestData.SLOT_START, TestData.SLOT_END, EventType.GYM,
                TestData.LIMIT_8, TestData.BOOKING_COUNT_1);
        when(repository.findSlotForUpdate(TestData.TIMESLOT_ID)).thenReturn(Optional.of(timeslot));
        doThrow(new DataIntegrityViolationException("empty")).when(repository).flush();

        assertThatThrownBy(() -> timeslotService.cancelTimeSlot(TestData.TIMESLOT_ID))
                .isInstanceOf(ServiceException.Conflict.class);
    }

    @Test
    void getAvailableTimeBookingWithSuccessForToday() {
        Timeslot timeslot = TestData.defaultTimeslot();
        try (MockedStatic<TimeUtil> timeMock = mockStatic(TimeUtil.class);
             MockedStatic<ExecutionContext> contextMock = mockStatic(ExecutionContext.class)) {
            timeMock.when(TimeUtil::now).thenReturn(TestData.NOW);
            ExecutionContext context = mock(ExecutionContext.class);
            contextMock.when(ExecutionContext::get).thenReturn(context);
            when(context.getUserID()).thenReturn(TestData.USER_ID);
            when(bookingClient.getAllByStatusShort(TestData.USER_ID, TestData.DATE_TODAY, EventType.GYM))
                    .thenReturn(List.of(TestData.TIMESLOT_ID));
            when(repository.findAllAvailableTimeslotsOnDay(eq(EventType.GYM), any(), any()))
                    .thenReturn(List.of(timeslot));

            List<TimeslotResponse> result =
                    timeslotService.getAvailableTimeBooking(TestData.DATE_TODAY, EventType.GYM);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).isBookedByMe()).isTrue();
        }
    }

    @Test
    void getAvailableTimeBookingWhenBookingClientFails() {
        Timeslot timeslot = TestData.defaultTimeslot();
        try (MockedStatic<TimeUtil> timeMock = mockStatic(TimeUtil.class);
             MockedStatic<ExecutionContext> contextMock = mockStatic(ExecutionContext.class)) {
            timeMock.when(TimeUtil::now).thenReturn(TestData.NOW);
            ExecutionContext context = mock(ExecutionContext.class);
            contextMock.when(ExecutionContext::get).thenReturn(context);
            when(context.getUserID()).thenReturn(TestData.USER_ID);
            when(bookingClient.getAllByStatusShort(any(), any(), any()))
                    .thenThrow(new RuntimeException("down"));
            when(repository.findAllAvailableTimeslotsOnDay(eq(EventType.GYM), any(), any()))
                    .thenReturn(List.of(timeslot));

            List<TimeslotResponse> result =
                    timeslotService.getAvailableTimeBooking(TestData.DATE_TODAY, EventType.GYM);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).isBookedByMe()).isFalse();
        }
    }

    @Test
    void getAvailableTimeBookingWhenDateInFuture() {
        try (MockedStatic<TimeUtil> timeMock = mockStatic(TimeUtil.class)) {
            timeMock.when(TimeUtil::now).thenReturn(TestData.NOW);
            LocalDate farFuture = TestData.DATE_TODAY.plusDays(30);

            assertThatThrownBy(() -> timeslotService.getAvailableTimeBooking(farFuture, EventType.GYM))
                    .isInstanceOf(ServiceException.BadRequest.class);
        }
    }

    @Test
    void getAvailableTimeBookingWhenDateInPast() {
        try (MockedStatic<TimeUtil> timeMock = mockStatic(TimeUtil.class)) {
            timeMock.when(TimeUtil::now).thenReturn(TestData.NOW);
            LocalDate past = TestData.DATE_TODAY.minusDays(1);

            assertThatThrownBy(() -> timeslotService.getAvailableTimeBooking(past, EventType.GYM))
                    .isInstanceOf(ServiceException.BadRequest.class);
        }
    }

    @Test
    void deleteTimeSlotsWithSuccess() {
        List<java.util.UUID> ids = List.of(TestData.TIMESLOT_ID, TestData.OTHER_TIMESLOT_ID);

        timeslotService.deleteTimeSlots(ids);

        verify(repository).deleteAllById(ids);
    }
}
