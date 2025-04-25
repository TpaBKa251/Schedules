package ru.tpu.hostel.schedules.aspect.amqp;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.amqp.core.Message;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
@Order(0)
public class RabbitTraceAspect {

    private final Tracer tracer;

    private final OpenTelemetry openTelemetry;

    private static final TextMapGetter<Message> GETTER = new TextMapGetter<>() {
        @Override
        public Iterable<String> keys(Message carrier) {
            return carrier != null
                    ? carrier.getMessageProperties().getHeaders().keySet()
                    : Collections.emptySet();
        }

        @Override
        public String get(Message carrier, String key) {
            if (carrier == null) return null;
            Object value = carrier.getMessageProperties().getHeaders().get(key);
            return value != null ? value.toString() : null;
        }
    };

    @Around("@annotation(org.springframework.amqp.rabbit.annotation.RabbitListener)")
    public Object traceRabbitListener(ProceedingJoinPoint joinPoint) throws Throwable {
        Message message = Arrays.stream(joinPoint.getArgs())
                .filter(Message.class::isInstance)
                .map(Message.class::cast)
                .findFirst()
                .orElse(null);

        if (message == null) {
            return joinPoint.proceed();
        }

        Context context = openTelemetry.getPropagators()
                .getTextMapPropagator()
                .extract(Context.current(), message, GETTER);

        String queue = getQueueName(message);
        Span span = tracer.spanBuilder("rabbit.receive")
                .setParent(context)
                .setAttribute("messaging.system", "rabbitmq")
                .setAttribute("messaging.destination", queue)
                .setAttribute("messaging.operation", "receive")
                .setAttribute(
                        "messaging.rabbitmq.routing_key",
                        message.getMessageProperties().getReceivedRoutingKey()
                )
                .startSpan();

        try (Scope ignored = span.makeCurrent()) {
            Object result = joinPoint.proceed();
            span.setStatus(StatusCode.OK);
            return result;
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }

    private String getQueueName(Message message) {
        return message.getMessageProperties().getConsumerQueue();
    }
}



