package ru.tpu.hostel.schedules.service.responsible.assigner.impl;

import org.junit.jupiter.api.Test;
import ru.tpu.hostel.schedules.TestData;
import ru.tpu.hostel.schedules.dto.request.ResponsibleSetRequestDto;
import ru.tpu.hostel.schedules.entity.EventType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class DefaultResponsibleAssignCheckerTest {

    private final DefaultResponsibleAssignChecker checker = new DefaultResponsibleAssignChecker();

    @Test
    void isApplicableWhenSoop() {
        assertThat(checker.isApplicable(EventType.SOOP)).isTrue();
    }

    @Test
    void isApplicableWhenNotSoop() {
        assertThat(checker.isApplicable(EventType.GYM)).isFalse();
    }

    @Test
    void canAssignResponsibleWithSuccess() {
        ResponsibleSetRequestDto dto =
                TestData.responsibleSetRequestDto(TestData.DATE_TODAY, EventType.SOOP, TestData.USER_ID);

        assertThatNoException().isThrownBy(() -> checker.canAssignResponsible(dto));
    }
}
