package ru.tpu.hostel.schedules.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tpu.hostel.schedules.repository.TimeSlotRepository;
import ru.tpu.hostel.schedules.schedules.TimeSlotSchedulesConfig;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Тесты генератора таймслотов {@link TimeSlotGenerator}
 */
@DisplayName("Тесты генератора таймслотов TimeSlotGenerator")
@ExtendWith(MockitoExtension.class)
class TimeSlotGeneratorTest {

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @InjectMocks
    private TimeSlotGenerator timeSlotGenerator;

    @AfterEach
    void commonVerify() {
        verifyNoMoreInteractions(timeSlotRepository);
    }

    @Test
    void generateMissingSlotsOnLastDayWithSuccess() {
        TimeSlotSchedulesConfig timeSlotSchedulesConfig = new TimeSlotSchedulesConfig();

        try (MockedStatic<TimeSlotSchedulesConfig> mockedSchedulesConfig = Mockito.mockStatic(TimeSlotSchedulesConfig.class)) {
            mockedSchedulesConfig.when(() -> TimeSlotSchedulesConfig.loadFromFile(anyString())).thenReturn(timeSlotSchedulesConfig);

        }
    }
}