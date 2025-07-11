//package ru.tpu.hostel.schedules.mapper;
//
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import ru.tpu.hostel.schedules.dto.response.TimeslotResponse;
//import ru.tpu.hostel.schedules.entity.Timeslot;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
///**
// * Тесты маппера для таймслотов {@link TimeslotMapper}
// */
//@DisplayName("Тесты маппера для таймслотов TimeSlotMapper")
//class TimeslotMapperTest {
//
//    @DisplayName("Тест успешного маппинга сущности в ДТО для ответа")
//    @Test
//    void mapTimeSlotToTimeSlotResponseWithSuccess() {
//        UUID uuid = UUID.randomUUID();
//        LocalDateTime now = LocalDateTime.now();
//        String timeRange = getTimeSlotTimeRange(now, now.plusHours(1));
//
//        Timeslot timeSlot = new Timeslot();
//        timeSlot.setId(uuid);
//        timeSlot.setStartTime(now);
//        timeSlot.setEndTime(now.plusHours(1));
//        TimeslotResponse expectedTimeslotResponse = new TimeslotResponse(uuid, timeRange);
//
//        TimeslotResponse actualTimeslotResponse = TimeslotMapper.mapTimeSlotToTimeSlotResponse(timeSlot);
//
//        assertEquals(expectedTimeslotResponse, actualTimeslotResponse);
//    }
//
//    private String getTimeSlotTimeRange(LocalDateTime start, LocalDateTime end) {
//        return formatTime(start) + " - " + formatTime(end);
//    }
//
//    private String formatTime(LocalDateTime dateTime) {
//        return dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
//    }
//
//}