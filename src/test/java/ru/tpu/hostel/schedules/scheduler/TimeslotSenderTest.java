package ru.tpu.hostel.schedules.scheduler;

import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tpu.hostel.internal.external.amqp.AmqpMessageSender;
import ru.tpu.hostel.internal.utils.TimeUtil;
import ru.tpu.hostel.schedules.TestData;
import ru.tpu.hostel.schedules.external.amqp.timeslot.TimeslotMessageType;
import ru.tpu.hostel.schedules.repository.TimeslotRepository;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeslotSenderTest {

    @Mock
    private TimeslotRepository timeslotRepository;

    @Mock
    private AmqpMessageSender amqpMessageSender;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OpenTelemetry openTelemetry;

    @InjectMocks
    private TimeslotSender timeslotSender;

    @Test
    void sendWithSuccess() {
        try (MockedStatic<TimeUtil> timeUtilMock = mockStatic(TimeUtil.class)) {
            timeUtilMock.when(TimeUtil::now).thenReturn(TestData.NOW);
            when(timeslotRepository.findAllByStartTimeAfter(TestData.NOW))
                    .thenReturn(List.of(TestData.defaultTimeslot()));

            timeslotSender.send();

            verify(amqpMessageSender).send(eq(TimeslotMessageType.TIMESLOTS), anyString(), any());
        }
    }
}
