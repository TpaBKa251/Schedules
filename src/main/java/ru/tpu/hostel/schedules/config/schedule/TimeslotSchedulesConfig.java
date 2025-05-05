package ru.tpu.hostel.schedules.config.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// sonar-ignore-file
@Data
public class TimeslotSchedulesConfig {

    private Map<String, Schedule> schedules;

    @Data
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

    @Data
    public static class TimeRange {
        private LocalTime start;
        private LocalTime end;
        private boolean endNextDay;
    }

    @Data
    public static class BreakConfig {
        private int afterSlots;
        private int breakDurationMinutes;
    }

    public static TimeslotSchedulesConfig loadFromFile(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper.readValue(new File(filePath).getAbsoluteFile(), TimeslotSchedulesConfig.class);
    }
}
