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

import static org.assertj.core.api.Assertions.assertThat;
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
                Data.TIME_GYM_1,
                Data.TIME_GYM_2,
                EventType.GYM,
                Data.LIMIT_5)
        );
        timeSlotRepository.save(Data.getNewTimeSlot(
                Data.TIME_GYM_2,
                Data.TIME_GYM_3,
                EventType.GYM,
                Data.LIMIT_5)
        );
        timeSlotRepository.save(Data.getNewTimeSlot(
                Data.TIME_GYM_3,
                Data.TIME_GYM_4,
                EventType.GYM,
                Data.LIMIT_5)
        );
        timeSlotRepository.save(Data.getNewTimeSlot(
                Data.TIME_KITCHEN_1,
                Data.TIME_KITCHEN_2,
                EventType.KITCHEN,
                Data.LIMIT_2)
        );
    }

    @Test
    void testFindLastByType() {
        TimeSlot expectedTimeSlot = Data.getNewTimeSlot(
                Data.TIME_GYM_3,
                Data.TIME_GYM_4,
                EventType.GYM,
                Data.LIMIT_5);

        Optional<TimeSlot> timeSlot = timeSlotRepository.findLastByType(EventType.GYM);

        assertThat(timeSlot.get())
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expectedTimeSlot);
    }

    @Test
    void testFindByTypeWithSingleTimeSlot() {
        TimeSlot expectedTimeSlot = Data.getNewTimeSlot(
                Data.TIME_KITCHEN_1,
                Data.TIME_KITCHEN_2,
                EventType.KITCHEN,
                Data.LIMIT_2);

        List<TimeSlot> timeSlots = timeSlotRepository.findByType(EventType.KITCHEN);

        assertThat(timeSlots)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(List.of(expectedTimeSlot));
    }

    @Test
    void testFindByTypeWithSomeTimeSlots() {
        TimeSlot expectedTimeSlot1 = Data.getNewTimeSlot(
                Data.TIME_GYM_1,
                Data.TIME_GYM_2,
                EventType.GYM,
                Data.LIMIT_5);

        TimeSlot expectedTimeSlot2 = Data.getNewTimeSlot(
                Data.TIME_GYM_2,
                Data.TIME_GYM_3,
                EventType.GYM,
                Data.LIMIT_5);

        TimeSlot expectedTimeSlot3 = Data.getNewTimeSlot(
                Data.TIME_GYM_3,
                Data.TIME_GYM_4,
                EventType.GYM,
                Data.LIMIT_5);

        List<TimeSlot> timeSlots = timeSlotRepository.findByType(EventType.GYM);

        assertThat(timeSlots)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(List.of(expectedTimeSlot1, expectedTimeSlot2, expectedTimeSlot3));
    }

    @Test
    void testFindAllByTypeAndStartTimeAfter() {
        LocalDateTime startTime = Data.TIME_GYM_2;

        TimeSlot expectedTimeSlot = Data.getNewTimeSlot(
                Data.TIME_GYM_3,
                Data.TIME_GYM_4,
                EventType.GYM,
                Data.LIMIT_5);

        List<TimeSlot> timeSlots = timeSlotRepository.findAllByTypeAndStartTimeAfter(EventType.GYM, startTime);

        assertThat(timeSlots)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(List.of(expectedTimeSlot));
    }

    @Test
    void testFindEarlierStartTimeByTypeAndStartTimeOnSpecificDay() {
        LocalDateTime startTime = Data.START_TIME;
        LocalDateTime endTime = Data.END_TIME;

        Optional<LocalDateTime> time = timeSlotRepository.findEarlierStartTimeByTypeAndStartTimeOnSpecificDay(
                EventType.GYM,
                startTime,
                endTime);

        assertTrue(time.isPresent());
        assertEquals(Data.TIME_GYM_1, time.get());
    }
}
