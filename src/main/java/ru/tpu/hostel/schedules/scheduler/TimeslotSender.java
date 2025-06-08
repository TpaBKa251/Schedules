package ru.tpu.hostel.schedules.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.tpu.hostel.internal.external.amqp.AmqpMessageSender;
import ru.tpu.hostel.internal.utils.TimeUtil;
import ru.tpu.hostel.schedules.external.amqp.timeslot.TimeslotMessageType;
import ru.tpu.hostel.schedules.external.amqp.timeslot.dto.ScheduleResponse;
import ru.tpu.hostel.schedules.external.amqp.timeslot.mapper.ScheduleResponseMapper;
import ru.tpu.hostel.schedules.repository.TimeslotRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimeslotSender {

    private final TimeslotRepository timeslotRepository;

    private final AmqpMessageSender amqpMessageSender;

    @Async
    public void send() {
        List<ScheduleResponse> timeslots = timeslotRepository.findAllByStartTimeAfter(TimeUtil.now()).stream()
                .map(ScheduleResponseMapper::mapTimeslotResponse)
                .toList();
        amqpMessageSender.send(
                TimeslotMessageType.TIMESLOTS,
                UUID.randomUUID().toString(),
                timeslots
        );
    }
}
