package ru.tpu.hostel.schedules.config.amqp.timeslot;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Свойства очереди для отправки сообщений микросервису расписаний и получения от него ответа по RabbitMQ
 */
@Validated
@ConfigurationProperties(prefix = "queueing.timeslots")
public record RabbitBookTimeslotQueueingProperties(

        @NotEmpty
        String exchangeName,

        @NotEmpty
        String queueName,

        @NotEmpty
        String routingKey

) {
}
