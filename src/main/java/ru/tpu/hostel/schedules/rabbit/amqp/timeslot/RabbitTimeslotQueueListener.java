package ru.tpu.hostel.schedules.rabbit.amqp.timeslot;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.tpu.hostel.schedules.entity.TimeSlot;
import ru.tpu.hostel.schedules.exception.ServiceException;
import ru.tpu.hostel.schedules.rabbit.amqp.AmqpMessageSender;
import ru.tpu.hostel.schedules.rabbit.amqp.timeslot.dto.ScheduleResponse;
import ru.tpu.hostel.schedules.rabbit.amqp.timeslot.mapper.ScheduleResponseMapper;
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
    @Transactional
    public void receiveTimeslotMessage(Message message) throws IOException {
        MessageProperties messageProperties = message.getMessageProperties();
        log.info("Received timeslot message: {}", messageProperties.getHeaders());
        ObjectMapper objectMapper = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        try {
            UUID timeslotId = objectMapper.readValue(message.getBody(), UUID.class);
            TimeSlot timeSlot = timeSlotService.bookTimeslot(timeslotId);
            ScheduleResponse timeSlotResponse = ScheduleResponseMapper.mapTimeslotResponse(timeSlot);
            timeslotAmqpMessageSender.sendReply(messageProperties, timeSlotResponse);
        } catch (Exception e) {
            ScheduleResponse failureResponse;
            if (e instanceof ServiceException serviceException) {
                failureResponse = ScheduleResponseMapper.mapFailureResponse(
                        serviceException.getStatus(),
                        serviceException.getMessage()
                );
            } else {
                failureResponse = ScheduleResponseMapper.mapFailureResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        e.getMessage()
                );
            }
            timeslotAmqpMessageSender.sendReply(messageProperties, failureResponse);
        }
    }

}
