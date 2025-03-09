package ru.tpu.hostel.schedules.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeNow {

    public static LocalDateTime now() {
        return ZonedDateTime.now(ZoneId.of("UTC+7")).toLocalDateTime();
    }
}
