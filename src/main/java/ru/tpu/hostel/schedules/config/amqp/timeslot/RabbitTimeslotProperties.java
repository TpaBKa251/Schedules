package ru.tpu.hostel.schedules.config.amqp.timeslot;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Свойства для подключения к брокеру сообщений RabbitMQ
 */
@Validated
@ConfigurationProperties(prefix = "rabbitmq.timeslots")
public record RabbitTimeslotProperties(

        @NotEmpty
        String username,

        @NotEmpty
        String password,

        @NotEmpty
        String virtualHost,

        @NotEmpty
        String addresses,

        @NotNull
        @DurationUnit(ChronoUnit.MILLIS)
        Duration connectionTimeout
) {
}
