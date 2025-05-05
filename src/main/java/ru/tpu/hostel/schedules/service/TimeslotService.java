package ru.tpu.hostel.schedules.service;

import ru.tpu.hostel.schedules.dto.response.TimeslotResponse;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.entity.Timeslot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TimeslotService {

    Timeslot getTimeslotForBook(UUID id);

    void cancelTimeSlot(UUID slotId);

    List<TimeslotResponse> getAvailableTimeBooking(LocalDate date, EventType bookingType);

}
