package ru.tpu.hostel.schedules.config.amqp.timeslot;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "queueing.timeslot")
public record RabbitScheduleServiceTimeslotQueueingProperties(

        @NotEmpty
        String exchangeName,

        @NotEmpty
        String routingKey

) {
}
