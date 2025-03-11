package ru.tpu.hostel.schedules.amqp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import ru.tpu.hostel.schedules.config.amqp.RabbitTimeslotQueueingProperties;
import ru.tpu.hostel.schedules.utils.TimeNow;

import java.time.ZonedDateTime;
import java.util.Date;

public class RabbitTimeslotSender implements AmqpMessageSender {

    private static final int HIGH_PRIORITY = 1;

    private static final ObjectWriter WRITER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
            .disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
            .setTimeZone(TimeNow.getTimeZone())
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writer();

    private final RabbitTemplate timeslotQueueRabbitTemplate;

    public RabbitTimeslotSender(
            ConnectionFactory schedulesServiceConnectionFactory,
            RabbitTimeslotQueueingProperties properties
    ) {
        this.timeslotQueueRabbitTemplate = new RabbitTemplate(schedulesServiceConnectionFactory);
        this.timeslotQueueRabbitTemplate.setExchange(properties.getExchangeName());
        this.timeslotQueueRabbitTemplate.setRoutingKey(properties.getRoutingKey());
    }

    @Override
    public void sendReply(MessageProperties receivedMessageProperties, Object messagePayload)
            throws JsonProcessingException {
        MessageProperties replyProperties = getMessageProperties(
                receivedMessageProperties.getMessageId(),
                receivedMessageProperties.getCorrelationId()
        );
        Message replyMessage = new Message(WRITER.writeValueAsBytes(messagePayload), replyProperties);
        timeslotQueueRabbitTemplate.send(receivedMessageProperties.getReplyTo(), replyMessage);
    }

    private MessageProperties getMessageProperties(String messageId, String correlationId) {
        ZonedDateTime now = TimeNow.getZonedDateTime();
        long nowMillis = now.toInstant().toEpochMilli();

        return MessagePropertiesBuilder.newInstance()
                .setMessageId(messageId)
                .setCorrelationId(correlationId)
                .setPriority(HIGH_PRIORITY)
                .setTimestamp(new Date(nowMillis))
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();
    }
}
