package ru.tpu.hostel.schedules.mapper;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import ru.tpu.hostel.internal.utils.TimeUtil;
import ru.tpu.hostel.schedules.TestData;
import ru.tpu.hostel.schedules.dto.response.ActiveEventResponseDto;
import ru.tpu.hostel.schedules.dto.response.KitchenScheduleResponseDto;
import ru.tpu.hostel.schedules.dto.response.KitchenScheduleShortResponseDto;
import ru.tpu.hostel.schedules.entity.KitchenSchedule;
import ru.tpu.hostel.schedules.external.rest.user.dto.UserResponseDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

class KitchenScheduleMapperTest {

    @Test
    void mapToKitchenScheduleShortResponseDtoWithSuccess() {
        KitchenSchedule kitchenSchedule = TestData.defaultKitchenSchedule();

        KitchenScheduleShortResponseDto response =
                KitchenScheduleMapper.mapToKitchenScheduleShortResponseDto(kitchenSchedule);

        assertThat(response.id()).isEqualTo(TestData.KITCHEN_ID);
        assertThat(response.date()).isEqualTo(TestData.DATE_TODAY);
        assertThat(response.roomNumber()).isEqualTo(TestData.ROOM_NUMBER_201);
        assertThat(response.checked()).isFalse();
    }

    @Test
    void mapToKitchenScheduleResponseDtoWithSuccess() {
        KitchenSchedule kitchenSchedule = TestData.defaultKitchenSchedule();
        List<UserResponseDto> users = List.of(TestData.userResponseDto());

        KitchenScheduleResponseDto response =
                KitchenScheduleMapper.mapToKitchenScheduleResponseDto(kitchenSchedule, users);

        assertThat(response.id()).isEqualTo(TestData.KITCHEN_ID);
        assertThat(response.users()).hasSize(1);
        assertThat(response.users().get(0).firstName()).isEqualTo(TestData.FIRST_NAME_IVAN);
        assertThat(response.users().get(0).tgLink()).isEqualTo(TestData.TG_LINK);
    }

    @Test
    void mapToActiveEventDtoWhenInProgress() {
        KitchenSchedule kitchenSchedule = TestData.defaultKitchenSchedule();

        try (MockedStatic<TimeUtil> timeMock = mockStatic(TimeUtil.class)) {
            timeMock.when(TimeUtil::now).thenReturn(TestData.NOW);

            ActiveEventResponseDto response = KitchenScheduleMapper.mapToActiveEventDto(kitchenSchedule);

            assertThat(response.id()).isEqualTo(TestData.KITCHEN_ID);
            assertThat(response.status()).isEqualTo("IN_PROGRESS");
            assertThat(response.type()).isEqualTo("Дежурство на кухне");
            assertThat(response.startTime()).isEqualTo(TestData.DATE_TODAY.atTime(0, 0));
            assertThat(response.endTime()).isEqualTo(TestData.DATE_TODAY.atTime(23, 59));
        }
    }

    @Test
    void mapToActiveEventDtoWhenBooked() {
        KitchenSchedule kitchenSchedule = TestData.defaultKitchenSchedule();

        try (MockedStatic<TimeUtil> timeMock = mockStatic(TimeUtil.class)) {
            timeMock.when(TimeUtil::now).thenReturn(TestData.NOW.minusDays(1));

            ActiveEventResponseDto response = KitchenScheduleMapper.mapToActiveEventDto(kitchenSchedule);

            assertThat(response.status()).isEqualTo("BOOKED");
        }
    }
}
