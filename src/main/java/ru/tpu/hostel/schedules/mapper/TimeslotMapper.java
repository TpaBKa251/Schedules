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
    public static TimeslotResponse mapTimeSlotToTimeSlotResponse(Timeslot timeSlot, boolean isBookedByMe) {
        return new TimeslotResponse(
                timeSlot.getId(),
                formatTime(timeSlot.getStartTime()),
                formatTime(timeSlot.getEndTime()),
                timeSlot.getBookingCount(),
                timeSlot.getLimit(),
                isBookedByMe
        );
    }

    private static String formatTime(LocalDateTime dateTime) {
        return dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

}
