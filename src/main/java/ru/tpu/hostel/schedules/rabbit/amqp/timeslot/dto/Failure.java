package ru.tpu.hostel.schedules.rabbit.amqp.timeslot.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = true)
@Getter
public class Failure extends ScheduleResponse {

    private final HttpStatus httpStatus;

    private final String message;

    @JsonCreator
    public Failure(
            @JsonProperty("httpStatus") String httpStatus,
            @JsonProperty("message") String message
    ) {
        super(ResponseStatus.FAILURE);
        this.httpStatus = HttpStatus.valueOf(httpStatus);
        this.message = message;
    }
}
