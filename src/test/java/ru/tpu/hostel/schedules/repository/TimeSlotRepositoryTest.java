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
public class TimeSlotRepositoryTest {

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

        timeSlotRepository.save(getNewTimeSlot(
                LocalDateTime.of(2025, 3, 10, 9, 0),
                LocalDateTime.of(2025, 3, 10, 10, 30),
                EventType.GYM,
                5)
        );
        timeSlotRepository.save(getNewTimeSlot(
                LocalDateTime.of(2025, 3, 10, 10, 30),
                LocalDateTime.of(2025, 3, 10, 12, 0),
                EventType.GYM,
                5)
        );
        timeSlotRepository.save(getNewTimeSlot(
                LocalDateTime.of(2025, 3, 10, 12, 0),
                LocalDateTime.of(2025, 3, 10, 13, 30),
                EventType.GYM,
                5)
        );
        timeSlotRepository.save(getNewTimeSlot(
                LocalDateTime.of(2025, 5, 11, 13, 30),
                LocalDateTime.of(2025, 5, 11, 15, 0),
                EventType.KITCHEN,
                2)
        );
    }

    @Test
    void testFindLastByType() {
        TimeSlot testTimeSlot = getNewTimeSlot(
                LocalDateTime.of(2025, 3, 10, 12, 0),
                LocalDateTime.of(2025, 3, 10, 13, 30),
                EventType.GYM,
                5);

        Optional<TimeSlot> timeSlot = timeSlotRepository.findLastByType(EventType.GYM);

        assertTrue(timeSlot.isPresent());
        assertEquals(testTimeSlot.getStartTime(), timeSlot.get().getStartTime());
        assertEquals(testTimeSlot.getEndTime(), timeSlot.get().getEndTime());
        assertEquals(testTimeSlot.getType(), timeSlot.get().getType());
        assertEquals(testTimeSlot.getLimit(), timeSlot.get().getLimit());
    }

    @Test
    void testFindByTypeWithSingleTimeSlot() {
        TimeSlot testTimeSlot = getNewTimeSlot(
                LocalDateTime.of(2025, 5, 11, 13, 30),
                LocalDateTime.of(2025, 5, 11, 15, 0),
                EventType.KITCHEN,
                2);

        List<TimeSlot> timeSlots = timeSlotRepository.findByType(EventType.KITCHEN);

        assertEquals(1, timeSlots.size());
        assertEquals(testTimeSlot.getStartTime(), timeSlots.get(0).getStartTime());
    }

    @Test
    void testFindByTypeWithSomeTimeSlots() {
        TimeSlot testTimeSlot = getNewTimeSlot(
                LocalDateTime.of(2025, 3, 10, 9, 0),
                LocalDateTime.of(2025, 3, 10, 10, 30),
                EventType.GYM,
                5);

        List<TimeSlot> timeSlots = timeSlotRepository.findByType(EventType.GYM);

        assertEquals(3, timeSlots.size());
        assertEquals(testTimeSlot.getStartTime(), timeSlots.get(0).getStartTime());
    }

    @Test
    void testFindAllByTypeAndStartTimeAfter() {
        LocalDateTime startTime = LocalDateTime.of(2025, 3, 10, 10, 30);

        List<TimeSlot> timeSlots = timeSlotRepository.findAllByTypeAndStartTimeAfter(EventType.GYM, startTime);

        assertEquals(1, timeSlots.size());
    }

    @Test
    void testFindEarlierStartTimeByTypeAndStartTimeOnSpecificDay() {
        LocalDateTime startTime = LocalDateTime.of(2020, 1, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 1, 0, 0);

        Optional<LocalDateTime> time =
                timeSlotRepository.findEarlierStartTimeByTypeAndStartTimeOnSpecificDay(
                        EventType.GYM,
                        startTime,
                        endTime);

        assertTrue(time.isPresent());
        assertEquals(LocalDateTime.of(2025, 3, 10, 9, 0), time.get());
    }

    private TimeSlot getNewTimeSlot(LocalDateTime startTime, LocalDateTime endTime, EventType eventType, Integer limit) {
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setStartTime(startTime);
        timeSlot.setEndTime(endTime);
        timeSlot.setType(eventType);
        timeSlot.setLimit(limit);
        return timeSlot;
    }
}
