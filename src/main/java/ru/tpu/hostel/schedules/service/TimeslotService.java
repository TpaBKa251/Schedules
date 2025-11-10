package ru.tpu.hostel.schedules.service;

import ru.tpu.hostel.schedules.dto.response.TimeslotResponse;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.entity.Timeslot;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TimeslotService {

    /**
     * Получение временного интервала для брони
     *
     * @param slotId id временного интервала
     * @return сущность временного интервала
     */
    Timeslot getTimeslotForBook(UUID slotId);

    /**
     * Отмена временного интервала
     *
     * @param slotId id временного интервала
     */
    void cancelTimeSlot(UUID slotId);

    /**
     * Получение списка доступных временных промежутков на определенную дату в указанный Event
     *
     * @param date дата в виде {@code LocalDate}
     * @param bookingType тип ивента в виде {@link EventType}
     * @return список из {@link TimeslotResponse}
     */
    List<TimeslotResponse> getAvailableTimeBooking(LocalDate date, EventType bookingType);

}
