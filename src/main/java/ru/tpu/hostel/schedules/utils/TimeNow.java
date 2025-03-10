package ru.tpu.hostel.schedules.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeNow {

    private static final ZoneId UTC7_ZONE = ZoneId.of("UTC+7");

    public static LocalDateTime now() {
        return ZonedDateTime.now(UTC7_ZONE).toLocalDateTime();
    }

    public static TimeZone getTimeZone() {
        return TimeZone.getTimeZone(UTC7_ZONE);
    }

    public static ZonedDateTime getZonedDateTime() {
        return ZonedDateTime.now(UTC7_ZONE);
    }

}
