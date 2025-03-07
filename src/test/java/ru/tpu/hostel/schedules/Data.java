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

    public static TimeSlot getNewTimeSlot(LocalDateTime startTime, LocalDateTime endTime, EventType eventType, Integer limit) {
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setStartTime(startTime);
        timeSlot.setEndTime(endTime);
        timeSlot.setType(eventType);
        timeSlot.setLimit(limit);
        return timeSlot;
    }
}
