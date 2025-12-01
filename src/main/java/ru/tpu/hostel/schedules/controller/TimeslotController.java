package ru.tpu.hostel.schedules.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tpu.hostel.schedules.dto.response.TimeslotResponse;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.service.TimeslotService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class TimeslotController {

    private final TimeslotService timeslotService;

    @GetMapping("/available/timeslot/{date}/{bookingType}")
    public List<TimeslotResponse> getAvailableTimeBooking(
            @PathVariable LocalDate date,
            @PathVariable EventType bookingType
    ) {
        return timeslotService.getAvailableTimeBooking(date, bookingType);
    }

    @DeleteMapping("/available/timeslot")
    public void deleteTimeSlot(@PathVariable List<UUID> timeSlotIds)
    {
        timeslotService.deleteTimeSlots(timeSlotIds);
    }
}
