package ru.tpu.hostel.schedules.external.rest.booking;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.tpu.hostel.schedules.entity.EventType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@FeignClient(name = "booking-bookingservice", url = "${rest.base-url.booking-service}")
public interface BookingClient {

    @GetMapping("bookings/all/booked/timeslot-id")
    List<UUID> getAllByStatusShort(
            @RequestParam UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam EventType type
    );
}
