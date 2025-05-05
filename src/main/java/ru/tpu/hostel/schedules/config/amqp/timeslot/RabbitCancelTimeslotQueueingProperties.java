package ru.tpu.hostel.schedules.config.amqp.timeslot;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "queueing.cancel")
public record RabbitCancelTimeslotQueueingProperties(

        @NotEmpty
        String exchangeName,

        @NotEmpty
        String queueName,

        @NotEmpty
        String routingKey

) {
}
