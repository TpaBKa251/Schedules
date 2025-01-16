package ru.tpu.hostel.schedules.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeNow {
    public static LocalDateTime now() {
        return ZonedDateTime.now(ZoneId.of("UTC+7")).toLocalDateTime();
    }
}
