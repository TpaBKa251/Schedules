package ru.tpu.hostel.schedules.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.tpu.hostel.internal.common.logging.LogFilter;

@Configuration
@RequiredArgsConstructor
@LogFilter(enableMethodLogging = false)
public class SchedulerStartUp {

    private final TimeslotGenerator timeslotGenerator;

    private final KitchenScheduleGenerator kitchenScheduleGenerator;

    @Bean
    public ApplicationRunner initializeTimeSlots() {
        return args -> {
            timeslotGenerator.generateSlotsForWeek();
            kitchenScheduleGenerator.handleMissedSchedules();
            kitchenScheduleGenerator.checkSchedules();
        };
    }
}
