package ru.tpu.hostel.schedules.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SchedulerStartUpTest {

    @Mock
    private TimeslotGenerator timeslotGenerator;

    @Mock
    private KitchenScheduleGenerator kitchenScheduleGenerator;

    @InjectMocks
    private SchedulerStartUp schedulerStartUp;

    @Test
    void initializeTimeSlotsWithSuccess() throws Exception {
        ApplicationRunner runner = schedulerStartUp.initializeTimeSlots();

        runner.run(mock(ApplicationArguments.class));

        assertThat(runner).isNotNull();
        verify(timeslotGenerator).generateSlotsForWeek();
        verify(kitchenScheduleGenerator).handleMissedSchedules();
        verify(kitchenScheduleGenerator).checkSchedules();
    }
}
