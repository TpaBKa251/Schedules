package ru.tpu.hostel.schedules.amqp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.tpu.hostel.schedules.config.amqp.RabbitTimeslotQueueingProperties;
import ru.tpu.hostel.schedules.dto.response.TimeSlotResponse;
import ru.tpu.hostel.schedules.utils.TimeNow;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import static ru.tpu.hostel.schedules.config.amqp.RabbitTimeslotConfiguration.TIMESLOT_LISTENER;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitTimeslotQueueListener {

    private static final ObjectWriter WRITER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
            .disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
            .setTimeZone(TimeNow.getTimeZone())
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writer();

    private final RabbitTemplate timeslotQueueRabbitTemplate;

    private final RabbitTimeslotQueueingProperties rabbitTimeslotQueueingProperties;

    @Transactional
    @RabbitListener(
            queues = "${queueing.timeslots.queueName}",
            containerFactory = TIMESLOT_LISTENER
    )
    public void receiveTimeslotMessage(Message message) throws IOException {
        MessageProperties messageProperties = message.getMessageProperties();
        String correlationId = messageProperties.getCorrelationId();
        log.info("Получен запрос на слоты с correlationId: {}", correlationId);

        TimeSlotResponse timeSlotResponse = new TimeSlotResponse(
                UUID.fromString(messageProperties.getMessageId()),
                "Да хз я сколько время"
        );

        MessageProperties replyProperties = MessagePropertiesBuilder.newInstance()
                .setMessageId(messageProperties.getMessageId())
                .setCorrelationId(messageProperties.getCorrelationId())
                .setPriority(1)
                .setTimestamp(new Date())
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();

        Message replyMessage = new Message(WRITER.writeValueAsBytes(timeSlotResponse), replyProperties);

        log.info("Ответ пойдет по ключу: {}", messageProperties.getReplyTo());

        timeslotQueueRabbitTemplate.send(messageProperties.getReplyTo(), replyMessage);
        log.info("Отправлен ответ с таймслотами в очередь {}", message.getMessageProperties().getReplyTo());
    }
}
