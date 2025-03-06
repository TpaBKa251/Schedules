package ru.tpu.hostel.schedules.mapper;

import lombok.experimental.UtilityClass;
import ru.tpu.hostel.schedules.dto.response.TimeSlotResponse;
import ru.tpu.hostel.schedules.entity.TimeSlot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Маппер для таймслотов
 */
@UtilityClass
public class TimeSlotMapper {

    /**
     * Преобразует сущность слота в ДТО для ответа
     *
     * @param timeSlot - сущность слота
     * @return ДТО для ответа
     */
    public static TimeSlotResponse mapTimeSlotToTimeSlotResponse(TimeSlot timeSlot) {
        return new TimeSlotResponse(
                timeSlot.getId(),
                getTimeSlotTimeRange(timeSlot.getStartTime(), timeSlot.getEndTime())
        );
    }

    private static String getTimeSlotTimeRange(LocalDateTime start, LocalDateTime end) {
        return formatTime(start) + " - " + formatTime(end);
    }

    private static String formatTime(LocalDateTime dateTime) {
        return dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

}
