package ru.tpu.hostel.schedules.config.amqp;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Свойства очереди для отправки сообщений микросервису расписаний и получения от него ответа по RabbitMQ
 */
@Data
@Validated
@ConfigurationProperties(prefix = "queueing.timeslots")
public class RabbitTimeslotQueueingProperties {

    /**
     * Имя обменника
     */
    @NotEmpty
    private String exchangeName;

    /**
     * Имя очереди
     */
    @NotEmpty
    private String queueName;

    /**
     * Имя ключа маршрутизации
     */
    @NotEmpty
    private String routingKey;

}
