package ru.tpu.hostel.schedules.amqp;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.amqp.core.MessageProperties;

public interface AmqpMessageSender {

    void sendReply(MessageProperties receivedMessageProperties, Object messagePayload) throws JsonProcessingException;
}
