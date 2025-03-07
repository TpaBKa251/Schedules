package ru.tpu.hostel.schedules;

import ru.tpu.hostel.schedules.entity.TimeSlot;
import ru.tpu.hostel.schedules.enums.EventType;

import java.time.LocalDateTime;

public class Data {
    public static final int YEAR_2020 = 2020;
    public static final int YEAR_2025 = 2025;
    public static final int YEAR_2026 = 2026;
    public static final int MONTH_1 = 1;
    public static final int MONTH_3 = 3;
    public static final int MONTH_5 = 5;
    public static final int DAY_1 = 1;
    public static final int DAY_10 = 10;
    public static final int DAY_11 = 11;
    public static final int HOUR_0 = 0;
    public static final int HOUR_9 = 9;
    public static final int HOUR_10 = 10;
    public static final int HOUR_12 = 12;
    public static final int HOUR_13 = 13;
    public static final int HOUR_15 = 15;
    public static final int MINUTE_0 = 0;
    public static final int MINUTE_30 = 30;
    public static final int LIMIT_2 = 2;
    public static final int LIMIT_5 = 5;
    public static final LocalDateTime TIME_GYM_1 = LocalDateTime.of(
            Data.YEAR_2025,
            Data.MONTH_3,
            Data.DAY_10,
            Data.HOUR_9,
            Data.MINUTE_0);
    public static final LocalDateTime TIME_GYM_2 = LocalDateTime.of(
            Data.YEAR_2025,
            Data.MONTH_3,
            Data.DAY_10,
            Data.HOUR_10,
            Data.MINUTE_30);
    public static final LocalDateTime TIME_GYM_3 = LocalDateTime.of(
            Data.YEAR_2025,
            Data.MONTH_3,
            Data.DAY_10,
            Data.HOUR_12,
            Data.MINUTE_0);
    public static final LocalDateTime TIME_GYM_4 = LocalDateTime.of(
            Data.YEAR_2025,
            Data.MONTH_3,
            Data.DAY_10,
            Data.HOUR_13,
            Data.MINUTE_30);
    public static final LocalDateTime TIME_KITCHEN_1 = LocalDateTime.of(
            Data.YEAR_2025,
            Data.MONTH_5,
            Data.DAY_11,
            Data.HOUR_13,
            Data.MINUTE_30);
    public static final LocalDateTime TIME_KITCHEN_2 = LocalDateTime.of(
            Data.YEAR_2025,
            Data.MONTH_5,
            Data.DAY_11,
            Data.HOUR_15,
            Data.MINUTE_0);
    public static final LocalDateTime START_TIME = LocalDateTime.of(
            Data.YEAR_2020,
            Data.MONTH_1,
            Data.DAY_1,
            Data.HOUR_0,
            Data.MINUTE_0);
    public static final LocalDateTime END_TIME = LocalDateTime.of(
            Data.YEAR_2026,
            Data.MONTH_1,
            Data.DAY_1,
            Data.HOUR_0,
            Data.MINUTE_0);


    public static TimeSlot getNewTimeSlot(
            LocalDateTime startTime,
            LocalDateTime endTime,
            EventType eventType,
            Integer limit
    ) {
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setStartTime(startTime);
        timeSlot.setEndTime(endTime);
        timeSlot.setType(eventType);
        timeSlot.setLimit(limit);
        return timeSlot;
    }
}
