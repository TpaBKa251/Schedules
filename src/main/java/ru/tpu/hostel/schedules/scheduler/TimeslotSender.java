package ru.tpu.hostel.schedules.scheduler;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.tpu.hostel.internal.external.amqp.AmqpMessageSender;
import ru.tpu.hostel.internal.utils.TimeUtil;
import ru.tpu.hostel.schedules.external.amqp.timeslot.TimeslotMessageType;
import ru.tpu.hostel.schedules.external.amqp.timeslot.dto.ScheduleResponse;
import ru.tpu.hostel.schedules.external.amqp.timeslot.mapper.ScheduleResponseMapper;
import ru.tpu.hostel.schedules.repository.TimeslotRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimeslotSender {

    private static final String SCOPE_NAME = "ru.tpu.hostel.schedules.scheduler.send.timeslots";

    private final TimeslotRepository timeslotRepository;

    private final AmqpMessageSender amqpMessageSender;

    private final OpenTelemetry openTelemetry;

    @Async
    public void send() {
        Span span = createSpan();

        try (Scope ignored = span.makeCurrent()) {
            SpanContext context = span.getSpanContext();
            MDC.put("traceId", context.getTraceId());
            MDC.put("spanId", context.getSpanId());

            List<ScheduleResponse> timeslots = timeslotRepository.findAllByStartTimeAfter(TimeUtil.now()).stream()
                    .map(ScheduleResponseMapper::mapTimeslotResponse)
                    .toList();
            amqpMessageSender.send(
                    TimeslotMessageType.TIMESLOTS,
                    UUID.randomUUID().toString(),
                    timeslots
            );

            span.setStatus(StatusCode.OK);
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR);
        } finally {
            MDC.clear();
            span.end();
        }

    }

    private Span createSpan() {
        return openTelemetry.getTracer(SCOPE_NAME)
                .spanBuilder("Send timeslots to Bookings")
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();
    }
}
