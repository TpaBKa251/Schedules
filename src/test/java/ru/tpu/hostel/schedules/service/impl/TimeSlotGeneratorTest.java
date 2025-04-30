package ru.tpu.hostel.schedules.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.tpu.hostel.schedules.entity.TimeSlot;
import ru.tpu.hostel.schedules.enums.EventType;
import ru.tpu.hostel.schedules.repository.TimeSlotRepository;
import ru.tpu.hostel.schedules.schedules.TimeSlotSchedulesConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static ru.tpu.hostel.schedules.Data.LIMIT;
import static ru.tpu.hostel.schedules.Data.TIME_GYM_1;
import static ru.tpu.hostel.schedules.Data.getNewTimeSlot;

/**
 * Тесты генератора таймслотов {@link TimeSlotGenerator}
 */
@DisplayName("Тесты генератора таймслотов TimeSlotGenerator")
@ExtendWith(MockitoExtension.class)
class TimeSlotGeneratorTest {

    private static final String FILE_NAME = "schedules.json";

    private static final String SCHEDULE_CONFIG = "configs/booking_schedules-test-configs.json";

    private static final String TIMESLOTS_CONFIG = "configs/timeslots-test-config.json";

    private static List<TimeSlotSchedulesConfig> configs;

    private static List<List<TimeSlot>> timeSlots;

    @Captor
    private ArgumentCaptor<List<TimeSlot>> slotCaptor;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @InjectMocks
    private TimeSlotGenerator timeSlotGenerator;

    @BeforeAll
    static void setUpFile() throws IOException {
        setUpConfigs();
    }

    @BeforeEach
    void setUp() throws IOException {
        ReflectionTestUtils.setField(timeSlotGenerator, "schedulesFilePath", FILE_NAME);
        setUpConfigs();
    }

    @AfterEach
    void commonVerify() {
        verifyNoMoreInteractions(timeSlotRepository);
    }

    @DisplayName("Тест успешной генерации слотов на последний день")
    @ParameterizedTest(name = "config: {0}")
    @MethodSource("argumentsForGenerateSlotsOnLastDayWithSuccess")
    void generateSlotsOnLastDayWithSuccess(
            TimeSlotSchedulesConfig config,
            List<TimeSlot> expectedTimeSlots,
            int expectedListLength) {
        try (MockedStatic<TimeSlotSchedulesConfig> mockedSchedulesConfig = mockStatic(TimeSlotSchedulesConfig.class);
             MockedStatic<TimeNow> mockedTimeNow = mockStatic(TimeNow.class)
        ) {
            mockedSchedulesConfig.when(() -> TimeSlotSchedulesConfig.loadFromFile(FILE_NAME))
                    .thenReturn(config);
            mockedTimeNow.when(TimeNow::now).thenReturn(TIME_GYM_1);

            timeSlotGenerator.generateSlotsOnLastDay();

            verify(timeSlotRepository).saveAll(slotCaptor.capture());
            List<TimeSlot> actualSlots = slotCaptor.getValue();
            expectedTimeSlots = expectedTimeSlots.subList(0, expectedListLength);
            assertThat(actualSlots)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .containsExactlyInAnyOrderElementsOf(expectedTimeSlots);
        }
    }

    @DisplayName("Тест генерации слотов на последний день, если загрузка конфига выкинула исключение")
    @Test
    void generateSlotsOnLastDayWithThrowingExceptionOnLoadingConfig() {
        try (MockedStatic<TimeSlotSchedulesConfig> mockedSchedulesConfig = mockStatic(TimeSlotSchedulesConfig.class)) {
            mockedSchedulesConfig.when(() -> TimeSlotSchedulesConfig.loadFromFile(FILE_NAME))
                    .thenThrow(IOException.class);

            assertThatNoException().isThrownBy(() -> timeSlotGenerator.generateSlotsOnLastDay());
        }
    }

    @DisplayName("Тест генерации слотов на последний день, если конфиг загрузился, но равен null")
    @Test
    void generateSlotsOnLastDayWithThrowingExceptionOnConfigIsNull() {
        try (MockedStatic<TimeSlotSchedulesConfig> mockedSchedulesConfig = mockStatic(TimeSlotSchedulesConfig.class)) {
            mockedSchedulesConfig.when(() -> TimeSlotSchedulesConfig.loadFromFile(FILE_NAME)).thenReturn(null);

            assertThatNoException().isThrownBy(() -> timeSlotGenerator.generateSlotsOnLastDay());
        }
    }

    @DisplayName("Тест успешной генерации слотов на неделю")
    @ParameterizedTest(name = "config: {0}")
    @MethodSource("argumentsForGenerateSlotsForWeekWithSuccess")
    void generateSlotsForWeekWithSuccess(TimeSlotSchedulesConfig config, List<TimeSlot> expectedTimeSlots) {
        try (MockedStatic<TimeSlotSchedulesConfig> mockedSchedulesConfig = mockStatic(TimeSlotSchedulesConfig.class);
             MockedStatic<TimeNow> mockedTimeNow = mockStatic(TimeNow.class)
        ) {
            mockedSchedulesConfig.when(() -> TimeSlotSchedulesConfig.loadFromFile(FILE_NAME))
                    .thenReturn(config);
            mockedTimeNow.when(TimeNow::now).thenReturn(TIME_GYM_1);
            when(timeSlotRepository.findLastByType(any(EventType.class))).thenReturn(Optional.empty());
            expectedTimeSlots = expectedTimeSlots.stream()
                    .peek(timeSlot -> {
                        timeSlot.setStartTime(timeSlot.getStartTime().minusDays(7));
                        timeSlot.setEndTime(timeSlot.getEndTime().minusDays(7));
                    }).toList();

            timeSlotGenerator.generateSlotsForWeek();

            verify(timeSlotRepository).saveAll(slotCaptor.capture());
            List<TimeSlot> actualSlots = slotCaptor.getValue();
            assertThat(actualSlots)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .containsExactlyInAnyOrderElementsOf(expectedTimeSlots);
        }
    }

    @DisplayName("Тест успешной генерации недостающих слотов на неделю")
    @ParameterizedTest(name = "config: {0}")
    @MethodSource("argumentsForGenerateSlotsForWeekWithSuccess")
    void generateSlotsForWeekWhenPartOfSlotsAlreadyGenerated(
            TimeSlotSchedulesConfig config,
            List<TimeSlot> expectedTimeSlots
    ) {
        try (MockedStatic<TimeSlotSchedulesConfig> mockedSchedulesConfig = mockStatic(TimeSlotSchedulesConfig.class);
             MockedStatic<TimeNow> mockedTimeNow = mockStatic(TimeNow.class)
        ) {
            mockedSchedulesConfig.when(() -> TimeSlotSchedulesConfig.loadFromFile(FILE_NAME))
                    .thenReturn(config);
            mockedTimeNow.when(TimeNow::now).thenReturn(TIME_GYM_1);
            TimeSlot lastTimeSlot = getNewTimeSlot(
                    TIME_GYM_1.plusDays(2),
                    TIME_GYM_1.plusDays(2),
                    EventType.HALL,
                    LIMIT
            );
            when(timeSlotRepository.findLastByType(any(EventType.class))).thenReturn(Optional.of(lastTimeSlot));
            expectedTimeSlots = expectedTimeSlots.stream()
                    .filter(timeSlot ->
                            !(timeSlot.getStartTime().toLocalDate().minusDays(7)
                                    .isEqual(TIME_GYM_1.plusDays(2).toLocalDate())
                                    || timeSlot.getStartTime().toLocalDate().minusDays(7)
                                    .isBefore(TIME_GYM_1.plusDays(2).toLocalDate()))
                    )
                    .peek(timeSlot -> {
                        timeSlot.setStartTime(timeSlot.getStartTime().minusDays(7));
                        timeSlot.setEndTime(timeSlot.getEndTime().minusDays(7));
                    }).toList();

            timeSlotGenerator.generateSlotsForWeek();

            verify(timeSlotRepository).saveAll(slotCaptor.capture());
            List<TimeSlot> actualSlots = slotCaptor.getValue();
            assertThat(actualSlots)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .containsExactlyInAnyOrderElementsOf(expectedTimeSlots);
        }
    }

    @DisplayName("Тест генерации слотов на неделю, если загрузка конфига выкинула исключение")
    @Test
    void generateSlotsForWeekWithThrowingExceptionOnLoadingConfig() {
        try (MockedStatic<TimeSlotSchedulesConfig> mockedSchedulesConfig = mockStatic(TimeSlotSchedulesConfig.class)) {
            mockedSchedulesConfig.when(() -> TimeSlotSchedulesConfig.loadFromFile(FILE_NAME))
                    .thenThrow(IOException.class);

            assertThatNoException().isThrownBy(() -> timeSlotGenerator.generateSlotsForWeek());
        }
    }

    @DisplayName("Тест генерации слотов на неделю, если конфиг загрузился, но равен null")
    @Test
    void generateSlotsForWeekWithThrowingExceptionOnConfigIsNull() {
        try (MockedStatic<TimeSlotSchedulesConfig> mockedSchedulesConfig = mockStatic(TimeSlotSchedulesConfig.class)) {
            mockedSchedulesConfig.when(() -> TimeSlotSchedulesConfig.loadFromFile(FILE_NAME)).thenReturn(null);

            assertThatNoException().isThrownBy(() -> timeSlotGenerator.generateSlotsForWeek());
        }
    }

    private static Stream<Arguments> argumentsForGenerateSlotsOnLastDayWithSuccess() {
        return Stream.of(
                Arguments.of(configs.get(0), timeSlots.get(0), 4),
                Arguments.of(configs.get(1), timeSlots.get(1), 6),
                Arguments.of(configs.get(2), timeSlots.get(2), 0),
                Arguments.of(configs.get(3), timeSlots.get(3), 0),
                Arguments.of(configs.get(4), timeSlots.get(4), 0),
                Arguments.of(configs.get(5), timeSlots.get(5), 4),
                Arguments.of(configs.get(6), timeSlots.get(6), 7),
                Arguments.of(configs.get(7), timeSlots.get(7), 0),
                Arguments.of(configs.get(8), timeSlots.get(8), 0),
                Arguments.of(configs.get(9), timeSlots.get(9), 0)
        );
    }

    private static Stream<Arguments> argumentsForGenerateSlotsForWeekWithSuccess() {
        return Stream.of(
                Arguments.of(configs.get(0), timeSlots.get(0)),
                Arguments.of(configs.get(1), timeSlots.get(1)),
                Arguments.of(configs.get(2), timeSlots.get(2)),
                Arguments.of(configs.get(3), timeSlots.get(3)),
                Arguments.of(configs.get(4), timeSlots.get(4)),
                Arguments.of(configs.get(5), timeSlots.get(5)),
                Arguments.of(configs.get(6), timeSlots.get(6)),
                Arguments.of(configs.get(7), timeSlots.get(7)),
                Arguments.of(configs.get(8), timeSlots.get(8)),
                Arguments.of(configs.get(9), timeSlots.get(9))
        );
    }

    private static void setUpConfigs() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        try (InputStream inputStream = TimeSlotGeneratorTest.class.getClassLoader()
                .getResourceAsStream(SCHEDULE_CONFIG)) {
            configs = objectMapper.readValue(inputStream, new TypeReference<>() {
            });
        }
        try (InputStream inputStream = TimeSlotGeneratorTest.class.getClassLoader()
                .getResourceAsStream(TIMESLOTS_CONFIG)) {
            timeSlots = objectMapper.readValue(inputStream, new TypeReference<>() {
            });
        }
    }
}