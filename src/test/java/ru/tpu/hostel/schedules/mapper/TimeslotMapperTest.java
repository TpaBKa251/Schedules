package ru.tpu.hostel.schedules.mapper;

import org.junit.jupiter.api.Test;
import ru.tpu.hostel.schedules.TestData;
import ru.tpu.hostel.schedules.dto.response.TimeslotResponse;
import ru.tpu.hostel.schedules.entity.Timeslot;

import static org.assertj.core.api.Assertions.assertThat;

class TimeslotMapperTest {

    @Test
    void mapTimeSlotToTimeSlotResponseWithSuccess() {
        Timeslot timeslot = TestData.defaultTimeslot();

        TimeslotResponse response = TimeslotMapper.mapTimeSlotToTimeSlotResponse(timeslot, true);

        assertThat(response.id()).isEqualTo(TestData.TIMESLOT_ID);
        assertThat(response.startTime()).isEqualTo("09:00");
        assertThat(response.endTime()).isEqualTo("09:45");
        assertThat(response.bookingCount()).isEqualTo(TestData.BOOKING_COUNT_0);
        assertThat(response.limit()).isEqualTo(TestData.LIMIT_8);
        assertThat(response.isBookedByMe()).isTrue();
    }

    @Test
    void mapTimeSlotToTimeSlotResponseWhenNotBookedByMe() {
        Timeslot timeslot = TestData.defaultTimeslot();

        TimeslotResponse response = TimeslotMapper.mapTimeSlotToTimeSlotResponse(timeslot, false);

        assertThat(response.isBookedByMe()).isFalse();
    }
}
