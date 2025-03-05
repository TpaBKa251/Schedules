package ru.tpu.hostel.schedules.schedules;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class SchedulesConfig {

    private Map<String, Schedule> schedules;

    @Getter
    @Setter
    public static class Schedule {
        private String type;
        private int limit;
        private UUID responsible;
        private List<String> workingDays;
        private TimeRange workingHours;
        private int slotDurationMinutes;
        private BreakConfig breaks;
        private Map<String, List<TimeRange>> reservedHours;
    }

    @Getter
    @Setter
    public static class TimeRange {
        private LocalTime start;
        private LocalTime end;
        private boolean endNextDay;
    }

    @Getter
    @Setter
    public static class BreakConfig {
        private int afterSlots;
        private int breakDurationMinutes;
    }
}
