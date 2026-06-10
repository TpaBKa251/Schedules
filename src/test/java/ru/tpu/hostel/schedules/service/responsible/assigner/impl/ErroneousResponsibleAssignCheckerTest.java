package ru.tpu.hostel.schedules.service.responsible.assigner.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ru.tpu.hostel.internal.exception.ServiceException;
import ru.tpu.hostel.schedules.TestData;
import ru.tpu.hostel.schedules.dto.request.ResponsibleSetRequestDto;
import ru.tpu.hostel.schedules.entity.EventType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ErroneousResponsibleAssignCheckerTest {

    private final ErroneousResponsibleAssignChecker checker = new ErroneousResponsibleAssignChecker();

    @ParameterizedTest
    @EnumSource(value = EventType.class, names = {"KITCHEN", "HALL", "INTERNET"})
    void isApplicableWhenApplicableType(EventType eventType) {
        assertThat(checker.isApplicable(eventType)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = EventType.class, names = {"GYM", "SOOP"})
    void isApplicableWhenNotApplicableType(EventType eventType) {
        assertThat(checker.isApplicable(eventType)).isFalse();
    }

    @Test
    void canAssignResponsibleAlwaysThrows() {
        ResponsibleSetRequestDto dto =
                TestData.responsibleSetRequestDto(TestData.DATE_TODAY, EventType.KITCHEN, TestData.USER_ID);

        assertThatThrownBy(() -> checker.canAssignResponsible(dto))
                .isInstanceOf(ServiceException.BadRequest.class);
    }
}
