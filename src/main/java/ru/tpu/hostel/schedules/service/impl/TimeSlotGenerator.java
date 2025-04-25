package ru.tpu.hostel.schedules.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.tpu.hostel.internal.utils.TimeUtil;
import ru.tpu.hostel.schedules.entity.TimeSlot;
import ru.tpu.hostel.schedules.enums.EventType;
import ru.tpu.hostel.schedules.repository.TimeSlotRepository;
import ru.tpu.hostel.schedules.schedules.TimeSlotSchedulesConfig;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Сервис для автоматической генерации и сохранения таймслотов
 */
@Slf4j
@Service
@RequiredArgsConstructor
@EnableScheduling
public class TimeSlotGenerator {

    /**
     * Файл-конфиг расписания для слотов
     */
    @Value("${schedules.timeslots.path}")
    private String schedulesFilePath;

    private static final String SCHEDULE_MAPPING_ERROR_LOG_MESSAGE
            = "Ошибка загрузки шаблона расписаний. Генерация слотов для брони невозможна";

    private static final int WEEK = 7;

    private static final int WEEK_PLUS_ONE_DAY = 8;

    private static final int ONE_DAY = 1;

    private final TimeSlotRepository timeSlotRepository;

    @Bean
    public ApplicationRunner initializeTimeSlots() {
        return args -> generateSlotsForWeek();
    }

    /**
     * Генерирует слот на последний день (через неделю)
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Tomsk")
    public void generateSlotsOnLastDay() {
        TimeSlotSchedulesConfig config;

        try {
            config = TimeSlotSchedulesConfig.loadFromFile(schedulesFilePath);
            if (config == null) {
                throw new IOException();
            }
        } catch (IOException e) {
            log.error(SCHEDULE_MAPPING_ERROR_LOG_MESSAGE + " (на последний день).", e);
            return;
        }

        List<TimeSlot> slots = new ArrayList<>();

        for (TimeSlotSchedulesConfig.Schedule schedule : config.getSchedules().values()) {
            List<DayOfWeek> workingDays = parseWorkingDays(schedule.getWorkingDays());
            Map<String, List<TimeSlotSchedulesConfig.TimeRange>> reservedHours = schedule.getReservedHours();

            LocalDate today = TimeUtil.now().toLocalDate();

            LocalDate currentDate = today.plusDays(WEEK);
            DayOfWeek currentDayOfWeek = currentDate.getDayOfWeek();

            if (workingDays.contains(currentDayOfWeek)) {
                List<TimeSlot> dailySlots = generateSlotsOnDay(
                        currentDate,
                        reservedHours.get(currentDayOfWeek.name()),
                        schedule
                );

                slots.addAll(dailySlots);
            }
        }

        timeSlotRepository.saveAll(slots);
    }

    /**
     * Генерирует слоты на неделю или добавляет недостающие
     */
    public void generateSlotsForWeek() {
        TimeSlotSchedulesConfig config;

        try {
            config = TimeSlotSchedulesConfig.loadFromFile(schedulesFilePath);
            if (config == null) {
                throw new IOException();
            }
        } catch (IOException e) {
            log.error(SCHEDULE_MAPPING_ERROR_LOG_MESSAGE + " (на неделю/недостающие).", e);
            return;
        }

        List<TimeSlot> slots = new ArrayList<>();
        LocalDate endOfWeek = TimeUtil.now().toLocalDate().plusDays(WEEK_PLUS_ONE_DAY);

        for (TimeSlotSchedulesConfig.Schedule schedule : config.getSchedules().values()) {
            List<DayOfWeek> workingDays = parseWorkingDays(schedule.getWorkingDays());
            Map<String, List<TimeSlotSchedulesConfig.TimeRange>> reservedHours = schedule.getReservedHours();

            TimeSlot lastSlot = timeSlotRepository.findLastByType(EventType.valueOf(schedule.getType()))
                    .orElse(null);

            LocalDate currentDate
                    = (lastSlot == null || lastSlot.getStartTime().toLocalDate().isBefore(TimeUtil.now().toLocalDate()))
                    ? TimeUtil.now().toLocalDate()
                    : lastSlot.getStartTime().toLocalDate().plusDays(ONE_DAY);

            while (currentDate.isBefore(endOfWeek)) {
                DayOfWeek currentDayOfWeek = currentDate.getDayOfWeek();

                if (workingDays.contains(currentDayOfWeek)) {
                    List<TimeSlot> dailySlots = generateSlotsOnDay(
                            currentDate,
                            reservedHours.get(currentDayOfWeek.name()),
                            schedule
                    );

                    slots.addAll(dailySlots);
                }

                currentDate = currentDate.plusDays(ONE_DAY);
            }
        }

        timeSlotRepository.saveAll(slots);
    }

    /**
     * Генерирует слоты на один день согласно расписанию
     *
     * @param date          дата, на которую генерируются слоты
     * @param reservedHours зарезервированные часы
     * @param schedule      расписание
     * @return список слотов на день
     */
    private List<TimeSlot> generateSlotsOnDay(
            LocalDate date,
            List<TimeSlotSchedulesConfig.TimeRange> reservedHours,
            TimeSlotSchedulesConfig.Schedule schedule
    ) {
        EventType type = EventType.valueOf(schedule.getType());
        int limit = schedule.getLimit();
        LocalTime startTime = schedule.getWorkingHours().getStart();
        LocalTime endTime = schedule.getWorkingHours().getEnd();
        boolean endNextDay = schedule.getWorkingHours().isEndNextDay();
        int slotDuration = schedule.getSlotDurationMinutes();
        TimeSlotSchedulesConfig.BreakConfig breaks = schedule.getBreaks();

        // TODO: добавить создание и сохранение ответственных после https://hostel-service.atlassian.net/browse/HOSTEL-2

        List<TimeSlot> dailySlots = new ArrayList<>();
        LocalDateTime slotStart = LocalDateTime.of(date, startTime);
        LocalDateTime workingEnd = endNextDay
                ? LocalDateTime.of(date.plusDays(1), endTime)
                : LocalDateTime.of(date, endTime);
        int slotsCounter = 0;

        while (slotStart.plusMinutes(slotDuration).isBefore(workingEnd)
                || slotStart.plusMinutes(slotDuration).equals(workingEnd)) {
            LocalDateTime slotEnd = slotStart.plusMinutes(slotDuration);

            final LocalDateTime finalSlotStart = slotStart;
            boolean overlapsReserved = reservedHours != null && reservedHours.stream()
                    .anyMatch(reserved ->
                            isOverlapping(finalSlotStart.toLocalTime(), slotEnd.toLocalTime(), reserved));

            if (!overlapsReserved) {
                TimeSlot timeSlot = new TimeSlot();
                timeSlot.setStartTime(slotStart);
                timeSlot.setEndTime(slotEnd);
                timeSlot.setType(type);
                timeSlot.setLimit(limit);
                timeSlot.setBookingCount(0);

                dailySlots.add(timeSlot);
                slotsCounter++;
            }

            TimeSlotSchedulesConfig.TimeRange matchingReserved = null;
            if (reservedHours != null) {
                matchingReserved = reservedHours.stream()
                        .filter(reserved ->
                                isSlotWithinReservedHours(finalSlotStart.toLocalTime(), slotEnd.toLocalTime(), reserved))
                        .findFirst()
                        .orElse(null);
            }

            slotStart = slotEnd;

            if (breaks != null
                    && breaks.getAfterSlots() > 0
                    && slotsCounter == breaks.getAfterSlots()
                    && matchingReserved == null) {
                slotStart = slotStart.plusMinutes(breaks.getBreakDurationMinutes());
                slotsCounter = 0;
            } else if (matchingReserved != null) {
                slotStart = LocalDateTime.of(slotStart.toLocalDate(), matchingReserved.getEnd());
                slotsCounter = 0;
            }
        }

        return dailySlots;
    }

    /**
     * Проверяет, не пересекает ли слот зарезервированные часы
     *
     * @param slotStart стартовое время слота
     * @param slotEnd   конечное время слота
     * @param reserved  зарезервированный промежуток времени
     * @return результат проверки. {@code true} - если пересекает, иначе {@code false}
     */
    private boolean isOverlapping(LocalTime slotStart, LocalTime slotEnd, TimeSlotSchedulesConfig.TimeRange reserved) {
        return slotStart.isBefore(reserved.getEnd()) && slotEnd.isAfter(reserved.getStart());
    }

    /**
     * Проверяет, попадает ли слот в зарезервированные часы (начало или конец).
     *
     * @param slotEnd  конечное время слота
     * @param reserved зарезервированный промежуток времени
     * @return результат проверки
     */
    private boolean isSlotWithinReservedHours(
            LocalTime slotStart,
            LocalTime slotEnd,
            TimeSlotSchedulesConfig.TimeRange reserved
    ) {
        return (slotStart.equals(reserved.getStart()) || slotEnd.equals(reserved.getEnd())) ||
                (slotStart.isAfter(reserved.getStart()) && slotStart.isBefore(reserved.getEnd())) ||
                (slotEnd.isAfter(reserved.getStart()) && slotEnd.isBefore(reserved.getEnd()));
    }

    /**
     * Преобразует строковый список рабочих дней в список из дней недели
     *
     * @param workingDays строковый список рабочих дней
     * @return список рабочих дней из дней недели
     */
    private List<DayOfWeek> parseWorkingDays(List<String> workingDays) {
        return workingDays.stream()
                .map(day -> DayOfWeek.valueOf(day.toUpperCase()))
                .toList();
    }
}
