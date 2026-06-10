package ru.tpu.hostel.schedules.service.editor.configs.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.tpu.hostel.schedules.TestData;
import ru.tpu.hostel.schedules.config.schedule.TimeslotSchedulesConfig;
import ru.tpu.hostel.schedules.dto.SchedulesDto;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.mapper.ScheduleMapper;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static ru.tpu.hostel.internal.exception.ServiceException.InternalServerError;

@ExtendWith(MockitoExtension.class)
class SchedulesEditorServiceImplTest {

    @Mock
    private ScheduleMapper scheduleMapper;

    @InjectMocks
    private SchedulesEditorServiceImpl schedulesEditorService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(schedulesEditorService, "schedulesFilePath", TestData.SCHEDULES_FILE_PATH);
    }

    @Test
    void editScheduleWithSuccess() {
        try (MockedStatic<TimeslotSchedulesConfig> configMock = mockStatic(TimeslotSchedulesConfig.class)) {
            configMock.when(() -> TimeslotSchedulesConfig.loadFromFile(any())).thenReturn(TestData.gymConfig());

            schedulesEditorService.editSchedule(TestData.schedulesDto(), EventType.GYM);

            configMock.verify(() -> TimeslotSchedulesConfig.loadToFile(any(), any()));
        }
    }

    @Test
    void editScheduleWhenLoadFails() {
        try (MockedStatic<TimeslotSchedulesConfig> configMock = mockStatic(TimeslotSchedulesConfig.class)) {
            configMock.when(() -> TimeslotSchedulesConfig.loadFromFile(any())).thenThrow(new IOException());

            assertThatThrownBy(() -> schedulesEditorService.editSchedule(TestData.schedulesDto(), EventType.GYM))
                    .isInstanceOf(InternalServerError.class);
        }
    }

    @Test
    void getScheduleWithSuccess() {
        try (MockedStatic<TimeslotSchedulesConfig> configMock = mockStatic(TimeslotSchedulesConfig.class)) {
            configMock.when(() -> TimeslotSchedulesConfig.loadFromFile(any())).thenReturn(TestData.gymConfig());
            when(scheduleMapper.mapToSchedulesDto(any())).thenReturn(TestData.schedulesDto());

            SchedulesDto result = schedulesEditorService.getSchedule(EventType.GYM);

            assertThat(result).isNotNull();
        }
    }

    @Test
    void getScheduleWhenLoadFails() {
        try (MockedStatic<TimeslotSchedulesConfig> configMock = mockStatic(TimeslotSchedulesConfig.class)) {
            configMock.when(() -> TimeslotSchedulesConfig.loadFromFile(any())).thenThrow(new IOException());

            assertThatThrownBy(() -> schedulesEditorService.getSchedule(EventType.GYM))
                    .isInstanceOf(InternalServerError.class);
        }
    }
}
