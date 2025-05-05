package ru.tpu.hostel.schedules.external.amqp.timeslot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import ru.tpu.hostel.schedules.entity.EventType;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Getter
public class TimeslotResponse extends ScheduleResponse {

    private final UUID id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime endTime;

    private final EventType type;

    public TimeslotResponse(UUID id, LocalDateTime startTime, LocalDateTime endTime, EventType type) {
        super(ResponseStatus.SUCCESS);
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
    }

}
