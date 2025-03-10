package ru.tpu.hostel.schedules.config.amqp;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "queueing.timeslots")
public class RabbitTimeslotQueueingProperties {

    @NotEmpty
    private String exchangeName;

    @NotEmpty
    private String queueName;

    @NotEmpty
    private String routingKey;

}
