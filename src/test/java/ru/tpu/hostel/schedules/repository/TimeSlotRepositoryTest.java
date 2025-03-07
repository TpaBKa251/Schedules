package ru.tpu.hostel.schedules.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tpu.hostel.schedules.Data;
import ru.tpu.hostel.schedules.entity.TimeSlot;
import ru.tpu.hostel.schedules.enums.EventType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Отключаем встраиваемую БД
class TimeSlotRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @BeforeEach
    void setUp() {
        timeSlotRepository.deleteAll();

        timeSlotRepository.save(Data.getNewTimeSlot(
                LocalDateTime.of(Data.YEAR_2025, Data.MONTH_3, Data.DAY_10, Data.HOUR_9, Data.MINUTE_0),
                LocalDateTime.of(Data.YEAR_2025, Data.MONTH_3, Data.DAY_10, Data.HOUR_10, Data.MINUTE_30),
                EventType.GYM,
                Data.LIMIT_5)
        );
        timeSlotRepository.save(Data.getNewTimeSlot(
                LocalDateTime.of(Data.YEAR_2025, Data.MONTH_3, Data.DAY_10, Data.HOUR_10, Data.MINUTE_30),
                LocalDateTime.of(Data.YEAR_2025, Data.MONTH_3, Data.DAY_10, Data.HOUR_12, Data.MINUTE_0),
                EventType.GYM,
                Data.LIMIT_5)
        );
        timeSlotRepository.save(Data.getNewTimeSlot(
                LocalDateTime.of(Data.YEAR_2025, Data.MONTH_3, Data.DAY_10, Data.HOUR_12, Data.MINUTE_0),
                LocalDateTime.of(Data.YEAR_2025, Data.MONTH_3, Data.DAY_10, Data.HOUR_13, Data.MINUTE_30),
                EventType.GYM,
                Data.LIMIT_5)
        );
        timeSlotRepository.save(Data.getNewTimeSlot(
                LocalDateTime.of(Data.YEAR_2025, Data.MONTH_5, Data.DAY_11, Data.HOUR_13, Data.MINUTE_30),
                LocalDateTime.of(Data.YEAR_2025, Data.MONTH_5, Data.DAY_11, Data.HOUR_15, Data.MINUTE_0),
                EventType.KITCHEN,
                Data.LIMIT_2)
        );
    }

    @Test
    void testFindLastByType() {
        TimeSlot testTimeSlot = Data.getNewTimeSlot(
                LocalDateTime.of(Data.YEAR_2025, Data.MONTH_3, Data.DAY_10, Data.HOUR_12, Data.MINUTE_0),
                LocalDateTime.of(Data.YEAR_2025, Data.MONTH_3, Data.DAY_10, Data.HOUR_13, Data.MINUTE_30),
                EventType.GYM,
                Data.LIMIT_5);

        Optional<TimeSlot> timeSlot = timeSlotRepository.findLastByType(EventType.GYM);

        assertTrue(timeSlot.isPresent());
        assertEquals(testTimeSlot.getStartTime(), timeSlot.get().getStartTime());
        assertEquals(testTimeSlot.getEndTime(), timeSlot.get().getEndTime());
        assertEquals(testTimeSlot.getType(), timeSlot.get().getType());
        assertEquals(testTimeSlot.getLimit(), timeSlot.get().getLimit());
    }

    @Test
    void testFindByTypeWithSingleTimeSlot() {
        TimeSlot testTimeSlot = Data.getNewTimeSlot(
                LocalDateTime.of(Data.YEAR_2025, Data.MONTH_5, Data.DAY_11, Data.HOUR_13, Data.MINUTE_30),
                LocalDateTime.of(Data.YEAR_2025, Data.MONTH_5, Data.DAY_11, Data.HOUR_15, Data.MINUTE_0),
                EventType.KITCHEN,
                Data.LIMIT_2);

        List<TimeSlot> timeSlots = timeSlotRepository.findByType(EventType.KITCHEN);

        assertEquals(1, timeSlots.size());
        assertEquals(testTimeSlot.getStartTime(), timeSlots.get(0).getStartTime());
    }

    @Test
    void testFindByTypeWithSomeTimeSlots() {
        TimeSlot testTimeSlot = Data.getNewTimeSlot(
                LocalDateTime.of(Data.YEAR_2025, Data.MONTH_3, Data.DAY_10, Data.HOUR_9, Data.MINUTE_0),
                LocalDateTime.of(Data.YEAR_2025, Data.MONTH_3, Data.DAY_10, Data.HOUR_10, Data.MINUTE_30),
                EventType.GYM,
                Data.LIMIT_5);

        List<TimeSlot> timeSlots = timeSlotRepository.findByType(EventType.GYM);

        assertEquals(3, timeSlots.size());
        assertEquals(testTimeSlot.getStartTime(), timeSlots.get(0).getStartTime());
    }

    @Test
    void testFindAllByTypeAndStartTimeAfter() {
        LocalDateTime startTime = LocalDateTime.of(
                Data.YEAR_2025,
                Data.MONTH_3,
                Data.DAY_10,
                Data.HOUR_10,
                Data.MINUTE_30);

        List<TimeSlot> timeSlots = timeSlotRepository.findAllByTypeAndStartTimeAfter(EventType.GYM, startTime);

        assertEquals(1, timeSlots.size());
    }

    @Test
    void testFindEarlierStartTimeByTypeAndStartTimeOnSpecificDay() {
        LocalDateTime startTime = LocalDateTime.of(Data.YEAR_2020, Data.MONTH_1, Data.DAY_1, Data.HOUR_0, Data.MINUTE_0);
        LocalDateTime endTime = LocalDateTime.of(Data.YEAR_2026, Data.MONTH_1, Data.DAY_1, Data.HOUR_0, Data.MINUTE_0);

        Optional<LocalDateTime> time =
                timeSlotRepository.findEarlierStartTimeByTypeAndStartTimeOnSpecificDay(
                        EventType.GYM,
                        startTime,
                        endTime);

        assertTrue(time.isPresent());
        assertEquals(
                LocalDateTime.of(Data.YEAR_2025, Data.MONTH_3, Data.DAY_10, Data.HOUR_9, Data.MINUTE_0),
                time.get());
    }
}
