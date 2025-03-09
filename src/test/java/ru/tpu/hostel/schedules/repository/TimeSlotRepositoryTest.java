package ru.tpu.hostel.schedules.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.tpu.hostel.schedules.Data;
import ru.tpu.hostel.schedules.entity.TimeSlot;
import ru.tpu.hostel.schedules.enums.EventType;
import ru.tpu.hostel.schedules.repository.util.RepositoryTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RepositoryTest
class TimeSlotRepositoryTest {

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @BeforeEach
    void setUp() {
        timeSlotRepository.deleteAll();

        timeSlotRepository.save(Data.getNewTimeSlot(
                Data.TIME_GYM_1,
                Data.TIME_GYM_2,
                EventType.GYM,
                Data.LIMIT_5
        ));
        timeSlotRepository.save(Data.getNewTimeSlot(
                Data.TIME_GYM_2,
                Data.TIME_GYM_3,
                EventType.GYM,
                Data.LIMIT_5
        ));
        timeSlotRepository.save(Data.getNewTimeSlot(
                Data.TIME_GYM_3,
                Data.TIME_GYM_4,
                EventType.GYM,
                Data.LIMIT_5
        ));
        timeSlotRepository.save(Data.getNewTimeSlot(
                Data.TIME_KITCHEN_1,
                Data.TIME_KITCHEN_2,
                EventType.KITCHEN,
                Data.LIMIT_2
        ));
    }

    @Test
    void testFindLastByType() {
        TimeSlot expectedTimeSlot = Data.getNewTimeSlot(
                Data.TIME_GYM_3,
                Data.TIME_GYM_4,
                EventType.GYM,
                Data.LIMIT_5
        );

        TimeSlot timeSlot = timeSlotRepository.findLastByType(EventType.GYM).orElseThrow();

        assertThat(timeSlot)
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
                Data.LIMIT_2
        );

        List<TimeSlot> timeSlots = timeSlotRepository.findByType(EventType.KITCHEN);

        assertThat(timeSlots)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .containsExactly(expectedTimeSlot);
    }

    @Test
    void testFindByTypeWithSomeTimeSlots() {
        TimeSlot expectedTimeSlot1 = Data.getNewTimeSlot(
                Data.TIME_GYM_1,
                Data.TIME_GYM_2,
                EventType.GYM,
                Data.LIMIT_5
        );
        TimeSlot expectedTimeSlot2 = Data.getNewTimeSlot(
                Data.TIME_GYM_2,
                Data.TIME_GYM_3,
                EventType.GYM,
                Data.LIMIT_5
        );
        TimeSlot expectedTimeSlot3 = Data.getNewTimeSlot(
                Data.TIME_GYM_3,
                Data.TIME_GYM_4,
                EventType.GYM,
                Data.LIMIT_5
        );
        List<TimeSlot> expectedTimeSlots = List.of(expectedTimeSlot1, expectedTimeSlot2, expectedTimeSlot3);

        List<TimeSlot> actualTimeSlots = timeSlotRepository.findByType(EventType.GYM);

        assertThat(actualTimeSlots)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .containsExactlyElementsOf(expectedTimeSlots);
    }

    @Test
    void testFindAllByTypeAndStartTimeAfter() {
        LocalDateTime startTime = Data.TIME_GYM_2;

        TimeSlot expectedTimeSlot = Data.getNewTimeSlot(
                Data.TIME_GYM_3,
                Data.TIME_GYM_4,
                EventType.GYM,
                Data.LIMIT_5
        );

        List<TimeSlot> timeSlots = timeSlotRepository.findAllByTypeAndStartTimeAfter(EventType.GYM, startTime);

        assertThat(timeSlots)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .containsExactly(expectedTimeSlot);
    }

    @Test
    void testFindEarlierStartTimeByTypeAndStartTimeOnSpecificDay() {
        LocalDateTime startTime = Data.START_TIME;
        LocalDateTime endTime = Data.END_TIME;

        LocalDateTime time = timeSlotRepository.findEarlierStartTimeByTypeAndStartTimeOnSpecificDay(
                EventType.GYM,
                startTime,
                endTime
        ).orElseThrow();


        assertEquals(Data.TIME_GYM_1, time);
    }
}
