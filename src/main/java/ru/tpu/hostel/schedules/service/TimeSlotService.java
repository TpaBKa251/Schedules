package ru.tpu.hostel.schedules.service;

import ru.tpu.hostel.schedules.dto.response.TimeSlotResponse;
import ru.tpu.hostel.schedules.entity.TimeSlot;

import java.util.UUID;

public interface TimeSlotService {

    TimeSlotResponse getTimeSlotById(UUID uuid);

    TimeSlot bookTimeslot(UUID id);
}
