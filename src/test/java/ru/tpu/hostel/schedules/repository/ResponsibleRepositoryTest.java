package ru.tpu.hostel.schedules.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.tpu.hostel.schedules.TestData;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.entity.Responsible;
import ru.tpu.hostel.schedules.repository.util.RepositoryTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RepositoryTest
class ResponsibleRepositoryTest {

    @Autowired
    private ResponsibleRepository responsibleRepository;

    private Responsible saved;

    @BeforeEach
    void setUp() {
        responsibleRepository.deleteAll();
        Responsible responsible = new Responsible();
        responsible.setType(EventType.GYM);
        responsible.setDate(TestData.DATE_TODAY);
        responsible.setUser(TestData.USER_ID);
        saved = responsibleRepository.save(responsible);
    }

    @Test
    void findByTypeAndDateWhenExists() {
        Optional<Responsible> result = responsibleRepository.findByTypeAndDate(EventType.GYM, TestData.DATE_TODAY);

        assertThat(result).isPresent();
    }

    @Test
    void findAllByTypeAndDateWithSuccess() {
        List<Responsible> result = responsibleRepository.findAllByTypeAndDate(EventType.GYM, TestData.DATE_TODAY);

        assertThat(result).hasSize(1);
    }

    @Test
    void findByUserTypeDateOptimisticWhenExists() {
        Optional<Responsible> result = responsibleRepository.findByUserTypeDateOptimistic(
                TestData.USER_ID, EventType.GYM, TestData.DATE_TODAY);

        assertThat(result).isPresent();
    }

    @Test
    void findByIdOptimisticWhenExists() {
        Optional<Responsible> result = responsibleRepository.findByIdOptimistic(saved.getId());

        assertThat(result).isPresent();
    }

    @Test
    void findAllActiveResponsibleWithSuccess() {
        List<Responsible> result = responsibleRepository.findAllActiveResponsible(
                TestData.USER_ID, TestData.DATE_TODAY.plusDays(1), TestData.DATE_TODAY);

        assertThat(result).hasSize(1);
    }
}
