package ru.tpu.hostel.schedules.config.amqp;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Data
@Validated
@ConfigurationProperties(prefix = "rabbitmq.timeslots")
public class RabbitTimeslotProperties {

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;

    @NotEmpty
    private String virtualHost;

    @NotEmpty
    private String addresses;

    @NotNull
    @DurationUnit(ChronoUnit.MILLIS)
    private Duration connectionTimeout;
}
