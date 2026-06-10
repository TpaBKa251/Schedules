package ru.tpu.hostel.schedules.scheduler;

import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tpu.hostel.internal.utils.TimeUtil;
import ru.tpu.hostel.schedules.TestData;
import ru.tpu.hostel.schedules.entity.KitchenSchedule;
import ru.tpu.hostel.schedules.repository.KitchenSchedulesRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KitchenScheduleGeneratorTest {

    @Mock
    private KitchenSchedulesRepository kitchenSchedulesRepository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OpenTelemetry openTelemetry;

    @InjectMocks
    private KitchenScheduleGenerator kitchenScheduleGenerator;

    @Test
    void handleMissedSchedulesWithSuccess() {
        KitchenSchedule schedule = TestData.newKitchenSchedule(
                TestData.KITCHEN_ID, TestData.DATE_TODAY, TestData.ROOM_NUMBER_201, false);
        when(kitchenSchedulesRepository.findAllByDateAndUnchecked(any(LocalDate.class)))
                .thenReturn(List.of(schedule));
        when(kitchenSchedulesRepository.findAllByFloorFromDate(anyString(), any(LocalDate.class)))
                .thenReturn(List.of(schedule));

        kitchenScheduleGenerator.handleMissedSchedules();

        verify(kitchenSchedulesRepository).findAllByFloorFromDate(anyString(), any(LocalDate.class));
    }

    @Test
    void checkSchedulesWhenSchedulesAlreadyExist() {
        try (MockedStatic<TimeUtil> timeUtilMock = mockStatic(TimeUtil.class)) {
            timeUtilMock.when(TimeUtil::now).thenReturn(TestData.NOW);
            when(kitchenSchedulesRepository.lockAllTable()).thenReturn(List.of());
            when(kitchenSchedulesRepository.findLastDateOfScheduleByFloor(anyInt()))
                    .thenReturn(Optional.of(TestData.DATE_TODAY));
            when(kitchenSchedulesRepository.findLastNumberOfScheduleByFloor(anyInt()))
                    .thenReturn(Optional.of(TestData.SCHEDULE_NUMBER_1));
            when(kitchenSchedulesRepository.findDateByRoomNumber(anyString())).thenReturn(Optional.empty());

            kitchenScheduleGenerator.checkSchedules();

            verify(kitchenSchedulesRepository).lockAllTable();
            assertThat(kitchenSchedulesRepository).isNotNull();
        }
    }
}
