package ru.tpu.hostel.schedules.rabbit.amqp;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.amqp.core.MessageProperties;

/**
 * Интерфейс отправителя сообщений в очередь RabbitMQ
 */
public interface AmqpMessageSender {

    /**
     * Отправляет ответ на сообщение
     *
     * @param receivedMessageProperties свойства полученного сообщения
     * @param messagePayload контент сообщения
     * @throws JsonProcessingException если не удалось преобразовать контент в JSON
     */
    void sendReply(MessageProperties receivedMessageProperties, Object messagePayload) throws JsonProcessingException;

}
