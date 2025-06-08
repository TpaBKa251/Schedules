package ru.tpu.hostel.schedules.external.amqp.timeslot.mapper;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import ru.tpu.hostel.schedules.entity.Timeslot;
import ru.tpu.hostel.schedules.external.amqp.timeslot.dto.Failure;
import ru.tpu.hostel.schedules.external.amqp.timeslot.dto.ScheduleResponse;
import ru.tpu.hostel.schedules.external.amqp.timeslot.dto.TimeslotResponse;

@UtilityClass
public class ScheduleResponseMapper {

    public static ScheduleResponse mapFailureResponse(HttpStatus httpStatus, String message) {
        return new Failure(httpStatus.name(), message);
    }

    public static ScheduleResponse mapTimeslotResponse(Timeslot timeSlot) {
        return new TimeslotResponse(
                timeSlot.getId(),
                timeSlot.getStartTime(),
                timeSlot.getEndTime(),
                timeSlot.getType(),
                timeSlot.getLimit(),
                timeSlot.getBookingCount()
        );
    }
}
