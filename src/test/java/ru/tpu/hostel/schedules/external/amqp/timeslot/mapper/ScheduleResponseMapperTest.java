package ru.tpu.hostel.schedules.external.amqp.timeslot.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import ru.tpu.hostel.schedules.TestData;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.entity.Timeslot;
import ru.tpu.hostel.schedules.external.amqp.timeslot.dto.Failure;
import ru.tpu.hostel.schedules.external.amqp.timeslot.dto.ResponseStatus;
import ru.tpu.hostel.schedules.external.amqp.timeslot.dto.ScheduleResponse;
import ru.tpu.hostel.schedules.external.amqp.timeslot.dto.TimeslotResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleResponseMapperTest {

    @Test
    void mapTimeslotResponseWithSuccess() {
        Timeslot timeslot = TestData.defaultTimeslot();

        ScheduleResponse response = ScheduleResponseMapper.mapTimeslotResponse(timeslot);

        assertThat(response).isInstanceOf(TimeslotResponse.class);
        assertThat(response.getStatus()).isEqualTo(ResponseStatus.SUCCESS);
        TimeslotResponse timeslotResponse = (TimeslotResponse) response;
        assertThat(timeslotResponse.getId()).isEqualTo(TestData.TIMESLOT_ID);
        assertThat(timeslotResponse.getStartTime()).isEqualTo(TestData.SLOT_START);
        assertThat(timeslotResponse.getEndTime()).isEqualTo(TestData.SLOT_END);
        assertThat(timeslotResponse.getType()).isEqualTo(EventType.GYM);
        assertThat(timeslotResponse.getLimit()).isEqualTo(TestData.LIMIT_8);
        assertThat(timeslotResponse.getBookingCount()).isEqualTo(TestData.BOOKING_COUNT_0);
    }

    @Test
    void mapFailureResponseWithSuccess() {
        ScheduleResponse response = ScheduleResponseMapper.mapFailureResponse(HttpStatus.CONFLICT, "msg");

        assertThat(response).isInstanceOf(Failure.class);
        assertThat(response.getStatus()).isEqualTo(ResponseStatus.FAILURE);
        Failure failure = (Failure) response;
        assertThat(failure.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(failure.getMessage()).isEqualTo("msg");
    }
}
