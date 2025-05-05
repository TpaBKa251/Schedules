package ru.tpu.hostel.schedules.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.tpu.hostel.schedules.Data;
import ru.tpu.hostel.schedules.entity.Timeslot;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.repository.util.RepositoryTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RepositoryTest
class TimeslotRepositoryTest {

    @Autowired
    private TimeslotRepository timeSlotRepository;

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
        Timeslot expectedTimeslot = Data.getNewTimeSlot(
                Data.TIME_GYM_3,
                Data.TIME_GYM_4,
                EventType.GYM,
                Data.LIMIT_5
        );

        Timeslot timeSlot = timeSlotRepository.findLastByType(EventType.GYM).orElseThrow();

        assertThat(timeSlot)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expectedTimeslot);
    }

    @Test
    void testFindByTypeWithSingleTimeSlot() {
        Timeslot expectedTimeslot = Data.getNewTimeSlot(
                Data.TIME_KITCHEN_1,
                Data.TIME_KITCHEN_2,
                EventType.KITCHEN,
                Data.LIMIT_2
        );

        List<Timeslot> timeslots = timeSlotRepository.findByType(EventType.KITCHEN);

        assertThat(timeslots)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .containsExactly(expectedTimeslot);
    }

    @Test
    void testFindByTypeWithSomeTimeSlots() {
        Timeslot expectedTimeslot1 = Data.getNewTimeSlot(
                Data.TIME_GYM_1,
                Data.TIME_GYM_2,
                EventType.GYM,
                Data.LIMIT_5
        );
        Timeslot expectedTimeslot2 = Data.getNewTimeSlot(
                Data.TIME_GYM_2,
                Data.TIME_GYM_3,
                EventType.GYM,
                Data.LIMIT_5
        );
        Timeslot expectedTimeslot3 = Data.getNewTimeSlot(
                Data.TIME_GYM_3,
                Data.TIME_GYM_4,
                EventType.GYM,
                Data.LIMIT_5
        );
        List<Timeslot> expectedTimeslots = List.of(expectedTimeslot1, expectedTimeslot2, expectedTimeslot3);

        List<Timeslot> actualTimeslots = timeSlotRepository.findByType(EventType.GYM);

        assertThat(actualTimeslots)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .containsExactlyElementsOf(expectedTimeslots);
    }

    @Test
    void testFindAllByTypeAndStartTimeAfter() {
        LocalDateTime startTime = Data.TIME_GYM_2;

        Timeslot expectedTimeslot = Data.getNewTimeSlot(
                Data.TIME_GYM_3,
                Data.TIME_GYM_4,
                EventType.GYM,
                Data.LIMIT_5
        );

        List<Timeslot> timeslots = timeSlotRepository.findAllByTypeAndStartTimeAfter(EventType.GYM, startTime);

        assertThat(timeslots)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .containsExactly(expectedTimeslot);
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
