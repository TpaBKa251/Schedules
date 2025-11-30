package ru.tpu.hostel.schedules.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tpu.hostel.internal.utils.TimeUtil;
import ru.tpu.hostel.schedules.config.schedule.RoomsConfig;
import ru.tpu.hostel.schedules.entity.KitchenSchedule;
import ru.tpu.hostel.schedules.repository.KitchenSchedulesRepository;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KitchenScheduleGenerator {

    private static final String SCOPE_NAME = "ru.tpu.hostel.schedules.scheduler.KitchenScheduleGenerator";

    @Value("${schedules.kitchen.path}")
    private String filePath;

    private final KitchenSchedulesRepository kitchenSchedulesRepository;

    private final OpenTelemetry openTelemetry;

    @Scheduled(cron = "0 0 12 * * *", zone = "Asia/Tomsk")
    @Transactional
    public void handleMissedSchedules() {
        Span span = createSpan("Handle missed kitchen duties");

        try (Scope ignored = span.makeCurrent()) {
            SpanContext context = span.getSpanContext();
            MDC.put("traceId", context.getTraceId());
            MDC.put("spanId", context.getSpanId());

            LocalDate yesterday = LocalDate.now().minusDays(1);

            List<KitchenSchedule> missedSchedules = kitchenSchedulesRepository
                    .findAllByDateAndUnchecked(yesterday);

            for (KitchenSchedule missedSchedule : missedSchedules) {
                String roomNumber = missedSchedule.getRoomNumber();
                String floor = String.valueOf(roomNumber.charAt(0));

                // Переносим все дежурства, включая пропущенное, на следующий день
                shiftFloorSchedules(floor, yesterday);
            }

            span.setStatus(StatusCode.OK);
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR);
        } finally {
            MDC.clear();
            span.end();
        }
    }

    private void shiftFloorSchedules(String floor, LocalDate fromDate) {
        List<KitchenSchedule> schedulesToShift = kitchenSchedulesRepository.findAllByFloorFromDate(floor, fromDate);

        for (KitchenSchedule schedule : schedulesToShift) {
            schedule.setDate(schedule.getDate().plusDays(1));
        }
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Tomsk")
    @Transactional
    public void checkSchedules() {
        Span span = createSpan("Check/generate kitchen schedule");

        try (Scope ignored = span.makeCurrent()) {
            SpanContext context = span.getSpanContext();
            MDC.put("traceId", context.getTraceId());
            MDC.put("spanId", context.getSpanId());

            kitchenSchedulesRepository.lockAllTable();
            for (int i = 2; i <= 5; i++) {
                LocalDate lastDate = kitchenSchedulesRepository.findLastDateOfScheduleByFloor(i).orElse(null);
                Integer lastNumber = kitchenSchedulesRepository.findLastNumberOfScheduleByFloor(i).orElse(null);

                if (lastDate == null || lastDate.isEqual(TimeUtil.now().toLocalDate().plusDays(2))) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.registerModule(new JavaTimeModule());

                    RoomsConfig roomsConfig;

                    try {
                        roomsConfig = objectMapper.readValue(new File(filePath), RoomsConfig.class);
                    } catch (IOException e) {
                        span.recordException(e);
                        span.setStatus(StatusCode.ERROR, "Не удалось загрузить список комнат");
                        return;
                    }

                    List<String> rooms = roomsConfig.toMap().get(String.valueOf(i));
                    List<KitchenSchedule> schedules = new ArrayList<>();

                    LocalDate scheduleDate = lastDate == null
                            ? TimeUtil.now().toLocalDate()
                            : TimeUtil.now().toLocalDate().plusDays(3);

                    for (String room : rooms) {
                        KitchenSchedule kitchenSchedule = new KitchenSchedule();

                        kitchenSchedule.setRoomNumber(room);
                        kitchenSchedule.setDate(scheduleDate);
                        kitchenSchedule.setScheduleNumber(lastNumber == null ? 1 : lastNumber + 1);
                        kitchenSchedule.setChecked(false);
                        schedules.add(kitchenSchedule);

                        scheduleDate = scheduleDate.plusDays(1);
                    }

                    kitchenSchedulesRepository.saveAll(schedules);
                }

                LocalDate date = kitchenSchedulesRepository.findDateByRoomNumber("202").orElse(null);

                if (date != null && TimeUtil.now().toLocalDate().equals(date)) {
                    kitchenSchedulesRepository.deleteAllByDateLessThan(date.minusDays(2));
                }
            }

            span.setStatus(StatusCode.OK);
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR);
        } finally {
            MDC.clear();
            span.end();
        }
    }

    private Span createSpan(String spanName) {
        return openTelemetry.getTracer(SCOPE_NAME)
                .spanBuilder(spanName)
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();
    }

}
