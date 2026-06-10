package ru.tpu.hostel.schedules.service.responsible.assigner.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tpu.hostel.internal.exception.ServiceException;
import ru.tpu.hostel.schedules.TestData;
import ru.tpu.hostel.schedules.dto.request.ResponsibleSetRequestDto;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.repository.TimeslotRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeslotResponsibleAssignCheckerTest {

    @Mock
    private TimeslotRepository timeSlotRepository;

    @InjectMocks
    private TimeslotResponsibleAssignChecker checker;

    @Test
    void isApplicableWhenGym() {
        assertThat(checker.isApplicable(EventType.GYM)).isTrue();
    }

    @Test
    void isApplicableWhenNotGym() {
        assertThat(checker.isApplicable(EventType.KITCHEN)).isFalse();
    }

    @Test
    void canAssignResponsibleWhenSlotsExist() {
        ResponsibleSetRequestDto dto =
                TestData.responsibleSetRequestDto(TestData.DATE_TODAY, EventType.GYM, TestData.USER_ID);
        when(timeSlotRepository.existsByTypeAndDate(eq(EventType.GYM), any(), any())).thenReturn(true);

        assertThatNoException().isThrownBy(() -> checker.canAssignResponsible(dto));
    }

    @Test
    void canAssignResponsibleWhenNoSlots() {
        ResponsibleSetRequestDto dto =
                TestData.responsibleSetRequestDto(TestData.DATE_TODAY, EventType.GYM, TestData.USER_ID);
        when(timeSlotRepository.existsByTypeAndDate(eq(EventType.GYM), any(), any())).thenReturn(false);

        assertThatThrownBy(() -> checker.canAssignResponsible(dto))
                .isInstanceOf(ServiceException.Conflict.class);
    }
}
