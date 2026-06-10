package ru.tpu.hostel.schedules.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.tpu.hostel.schedules.TestData;
import ru.tpu.hostel.schedules.entity.KitchenSchedule;
import ru.tpu.hostel.schedules.repository.util.RepositoryTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RepositoryTest
class KitchenSchedulesRepositoryTest {

    @Autowired
    private KitchenSchedulesRepository kitchenSchedulesRepository;

    private KitchenSchedule saved;

    @BeforeEach
    void setUp() {
        kitchenSchedulesRepository.deleteAll();
        saved = kitchenSchedulesRepository.save(buildSchedule(TestData.ROOM_NUMBER_201));
        kitchenSchedulesRepository.save(buildSchedule(TestData.ROOM_NUMBER_202));
    }

    private KitchenSchedule buildSchedule(String roomNumber) {
        KitchenSchedule schedule = new KitchenSchedule();
        schedule.setDate(TestData.DATE_TODAY);
        schedule.setRoomNumber(roomNumber);
        schedule.setChecked(false);
        schedule.setScheduleNumber(TestData.SCHEDULE_NUMBER_1);
        return schedule;
    }

    @Test
    void findAllAfterDateWithSuccess() {
        List<KitchenSchedule> result = kitchenSchedulesRepository.findAllAfterDate(TestData.DATE_TODAY);

        assertThat(result).hasSize(2);
    }

    @Test
    void findAllByDateEqualsWithSuccess() {
        List<KitchenSchedule> result = kitchenSchedulesRepository.findAllByDateEquals(TestData.DATE_TODAY);

        assertThat(result).hasSize(2);
    }

    @Test
    void findAllActiveDutiesWithSuccess() {
        List<KitchenSchedule> result = kitchenSchedulesRepository.findAllActiveDuties(
                TestData.ROOM_NUMBER_201, TestData.DATE_TODAY.plusDays(1), TestData.DATE_TODAY);

        assertThat(result).hasSize(1);
    }

    @Test
    void findByFloorAndDateWhenExists() {
        Optional<KitchenSchedule> result =
                kitchenSchedulesRepository.findByFloorAndDate(TestData.FLOOR_2, TestData.DATE_TODAY);

        assertThat(result).isPresent();
    }

    @Test
    void findAllOnFloorAfterDateWithSuccess() {
        List<KitchenSchedule> result =
                kitchenSchedulesRepository.findAllOnFloorAfterDate(TestData.FLOOR_2, TestData.DATE_TODAY);

        assertThat(result).hasSize(2);
    }

    @Test
    void findByIdForUpdateOptimisticWhenExists() {
        Optional<KitchenSchedule> result = kitchenSchedulesRepository.findByIdForUpdateOptimistic(saved.getId());

        assertThat(result).isPresent();
    }

    @Test
    void findDutiesForSwapWithSuccess() {
        KitchenSchedule other = kitchenSchedulesRepository.findAllAfterDate(TestData.DATE_TODAY).get(1);

        List<KitchenSchedule> result =
                kitchenSchedulesRepository.findDutiesForSwap(saved.getId(), other.getId());

        assertThat(result).hasSize(2);
    }
}
