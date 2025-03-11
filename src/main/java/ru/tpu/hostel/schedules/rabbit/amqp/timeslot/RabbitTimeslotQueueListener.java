package ru.tpu.hostel.schedules.rabbit.amqp.timeslot;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.tpu.hostel.schedules.dto.response.TimeSlotResponse;
import ru.tpu.hostel.schedules.rabbit.amqp.AmqpMessageSender;
import ru.tpu.hostel.schedules.service.TimeSlotService;

import java.io.IOException;
import java.util.UUID;

import static ru.tpu.hostel.schedules.config.amqp.RabbitTimeslotConfiguration.TIMESLOT_LISTENER;

/**
 * Слушатель RabbitMQ микросервиса броней
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitTimeslotQueueListener {

    private final AmqpMessageSender timeslotAmqpMessageSender;

    private final TimeSlotService timeSlotService;

    /**
     * Обрабатывает сообщения из очереди для таймслотов
     *
     * @param message сообщение, которое пришло
     * @throws IOException если произошла ошибка отправки
     */
    @RabbitListener(queues = "${queueing.timeslots.queueName}", containerFactory = TIMESLOT_LISTENER)
    public void receiveTimeslotMessage(Message message) throws IOException {
        MessageProperties messageProperties = message.getMessageProperties();
        ObjectMapper objectMapper = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        try {
            UUID timeslotId = objectMapper.readValue(message.getBody(), UUID.class);
            TimeSlotResponse timeSlotResponse = timeSlotService.getTimeSlotById(timeslotId);
            timeslotAmqpMessageSender.sendReply(messageProperties, timeSlotResponse);
        } catch (IOException e) {
            timeslotAmqpMessageSender.sendReply(messageProperties, null);
        }
    }

}
