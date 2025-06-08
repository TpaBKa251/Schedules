package ru.tpu.hostel.schedules.external.amqp.timeslot;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.tpu.hostel.internal.exception.ServiceException;
import ru.tpu.hostel.internal.external.amqp.AmqpMessageSender;
import ru.tpu.hostel.schedules.entity.Timeslot;
import ru.tpu.hostel.schedules.external.amqp.timeslot.dto.ScheduleResponse;
import ru.tpu.hostel.schedules.external.amqp.timeslot.mapper.ScheduleResponseMapper;
import ru.tpu.hostel.schedules.service.TimeslotService;

import java.io.IOException;
import java.util.UUID;

import static ru.tpu.hostel.schedules.config.amqp.timeslot.RabbitTimeslotConfiguration.TIMESLOT_LISTENER;

/**
 * Слушатель RabbitMQ микросервиса броней
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitTimeslotQueueListener {

    private final AmqpMessageSender amqpMessageSender;

    private final TimeslotService timeSlotService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    /**
     * Обрабатывает сообщения из очереди для таймслотов
     *
     * @param message сообщение, которое пришло
     * @throws IOException если произошла ошибка отправки
     */
    @RabbitListener(queues = "${queueing.book.queueName}", containerFactory = TIMESLOT_LISTENER)
    public void receiveTimeslotMessage(Message message) {
        MessageProperties messageProperties = message.getMessageProperties();

        try {
            UUID timeslotId = objectMapper.readValue(message.getBody(), UUID.class);
            Timeslot timeSlot = timeSlotService.getTimeslotForBook(timeslotId);
            ScheduleResponse timeSlotResponse = ScheduleResponseMapper.mapTimeslotResponse(timeSlot);
            amqpMessageSender.sendReply(TimeslotMessageType.BOOK_REPLY, messageProperties, timeSlotResponse);
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
            amqpMessageSender.sendReply(TimeslotMessageType.BOOK_REPLY, messageProperties, failureResponse);
        }
    }

    @RabbitListener(queues = "${queueing.cancel.queueName}", containerFactory = TIMESLOT_LISTENER)
    public void receiveCancelTimeslotMessage(Message message) {
        try {
            UUID timeslotId = objectMapper.readValue(message.getBody(), UUID.class);
            timeSlotService.cancelTimeSlot(timeslotId);
        } catch (IOException e) {
            throw new ServiceException.InternalServerError("Не удалось десериализовать сообщение");
        }
    }

}
