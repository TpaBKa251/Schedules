package ru.tpu.hostel.schedules.mapper;

import lombok.experimental.UtilityClass;
import ru.tpu.hostel.schedules.dto.response.TimeslotResponse;
import ru.tpu.hostel.schedules.entity.Timeslot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Маппер для таймслотов
 */
@UtilityClass
public class TimeslotMapper {

    /**
     * Преобразует сущность слота в ДТО для ответа
     *
     * @param timeSlot - сущность слота
     * @return ДТО для ответа
     */
    public static TimeslotResponse mapTimeSlotToTimeSlotResponse(Timeslot timeSlot) {
        return new TimeslotResponse(
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
