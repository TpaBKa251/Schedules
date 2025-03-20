package ru.tpu.hostel.schedules.aspect.amqp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class RabbitTimeslotQueueListenerLoggingAspect {

    private static final ObjectWriter WRITER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
            .disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .writer();

    @Around("execution(* ru.tpu.hostel.schedules.rabbit.amqp.timeslot.RabbitTimeslotQueueListener.receiveTimeslotMessage(..))")
    public Object logReceiveTimeslotMessage(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        if (args.length < 1 || !(args[0] instanceof Message message)) {
            log.warn("Неверные аргументы для слушателя");
            return joinPoint.proceed();
        }

        MessageProperties properties = message.getMessageProperties();
        String messageId = properties.getMessageId();
        String payloadJson = safeMapToJson(message.getBody());

        log.info("Получено сообщение: messageId={}, payload={}", messageId, payloadJson);

        Object result = joinPoint.proceed();

        log.info("Сообщение обработано: messageId={}", messageId);
        return result;
    }

    private String safeMapToJson(Object obj) {
        try {
            return WRITER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации в JSON: {}", e.getMessage(), e);
            return "<ошибка сериализации>";
        }
    }
}