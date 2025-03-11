package ru.tpu.hostel.schedules.amqp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.tpu.hostel.schedules.dto.response.TimeSlotResponse;

import java.io.IOException;
import java.util.UUID;

import static ru.tpu.hostel.schedules.config.amqp.RabbitTimeslotConfiguration.TIMESLOT_LISTENER;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitTimeslotQueueListener {

    private final AmqpMessageSender timeslotAmqpMessageSender;

    @RabbitListener(queues = "${queueing.timeslots.queueName}", containerFactory = TIMESLOT_LISTENER)
    public void receiveTimeslotMessage(Message message) throws IOException {
        MessageProperties messageProperties = message.getMessageProperties();
        TimeSlotResponse timeSlotResponse = new TimeSlotResponse(
                UUID.fromString(messageProperties.getMessageId()),
                "Да хз я сколько время"
        );
        timeslotAmqpMessageSender.sendReply(messageProperties, timeSlotResponse);
    }

}
