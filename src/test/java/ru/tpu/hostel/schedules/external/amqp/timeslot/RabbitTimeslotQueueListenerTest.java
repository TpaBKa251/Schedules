package ru.tpu.hostel.schedules.external.amqp.timeslot;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import ru.tpu.hostel.internal.exception.ServiceException;
import ru.tpu.hostel.internal.external.amqp.AmqpMessageSender;
import ru.tpu.hostel.schedules.TestData;
import ru.tpu.hostel.schedules.entity.Timeslot;
import ru.tpu.hostel.schedules.external.amqp.timeslot.dto.Failure;
import ru.tpu.hostel.schedules.external.amqp.timeslot.dto.TimeslotResponse;
import ru.tpu.hostel.schedules.service.TimeslotService;

import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RabbitTimeslotQueueListenerTest {

    @Mock
    private AmqpMessageSender amqpMessageSender;

    @Mock
    private TimeslotService timeSlotService;

    @InjectMocks
    private RabbitTimeslotQueueListener rabbitTimeslotQueueListener;

    @AfterEach
    void commonVerify() {
        verifyNoMoreInteractions(amqpMessageSender, timeSlotService);
    }

    @Test
    void receiveTimeslotMessageWithSuccess() {
        MessageProperties messageProperties = MessagePropertiesBuilder.newInstance().build();
        String body = "\"" + TestData.TIMESLOT_ID + "\"";
        Message message = MessageBuilder.withBody(body.getBytes()).andProperties(messageProperties).build();
        Timeslot timeslot = TestData.defaultTimeslot();
        when(timeSlotService.getTimeslotForBook(TestData.TIMESLOT_ID)).thenReturn(timeslot);

        rabbitTimeslotQueueListener.receiveTimeslotMessage(message);

        verify(timeSlotService).getTimeslotForBook(TestData.TIMESLOT_ID);
        verify(amqpMessageSender).sendReply(
                eq(TimeslotMessageType.BOOK_REPLY),
                eq(messageProperties),
                any(TimeslotResponse.class)
        );
    }

    @Test
    void receiveTimeslotMessageWhenServiceThrowsServiceException() {
        MessageProperties messageProperties = MessagePropertiesBuilder.newInstance().build();
        String body = "\"" + TestData.TIMESLOT_ID + "\"";
        Message message = MessageBuilder.withBody(body.getBytes()).andProperties(messageProperties).build();
        when(timeSlotService.getTimeslotForBook(TestData.TIMESLOT_ID))
                .thenThrow(new ServiceException.Conflict("conflict"));

        rabbitTimeslotQueueListener.receiveTimeslotMessage(message);

        verify(timeSlotService).getTimeslotForBook(TestData.TIMESLOT_ID);
        verify(amqpMessageSender).sendReply(
                eq(TimeslotMessageType.BOOK_REPLY),
                eq(messageProperties),
                any(Failure.class)
        );
    }

    @Test
    void receiveTimeslotMessageWhenDeserializationFails() {
        MessageProperties messageProperties = MessagePropertiesBuilder.newInstance().build();
        Message message = MessageBuilder.withBody("not-a-uuid".getBytes()).andProperties(messageProperties).build();

        rabbitTimeslotQueueListener.receiveTimeslotMessage(message);

        verify(amqpMessageSender).sendReply(
                eq(TimeslotMessageType.BOOK_REPLY),
                eq(messageProperties),
                any(Failure.class)
        );
    }

    @Test
    void receiveCancelTimeslotMessageWithSuccess() {
        String body = "\"" + TestData.TIMESLOT_ID + "\"";
        Message message = MessageBuilder.withBody(body.getBytes())
                .andProperties(MessagePropertiesBuilder.newInstance().build())
                .build();

        rabbitTimeslotQueueListener.receiveCancelTimeslotMessage(message);

        verify(timeSlotService).cancelTimeSlot(TestData.TIMESLOT_ID);
    }

    @Test
    void receiveCancelTimeslotMessageWhenDeserializationFails() {
        Message message = MessageBuilder.withBody("broken".getBytes())
                .andProperties(MessagePropertiesBuilder.newInstance().build())
                .build();

        assertThat(catchServiceException(message)).isInstanceOf(ServiceException.InternalServerError.class);
        verifyNoInteractions(timeSlotService);
    }

    private Throwable catchServiceException(Message message) {
        try {
            rabbitTimeslotQueueListener.receiveCancelTimeslotMessage(message);
            return null;
        } catch (Throwable t) {
            return t;
        }
    }

    @Test
    void failureResponseHasInternalServerErrorForNonServiceException() {
        MessageProperties messageProperties = MessagePropertiesBuilder.newInstance().build();
        String body = "\"" + TestData.TIMESLOT_ID + "\"";
        Message message = MessageBuilder.withBody(body.getBytes()).andProperties(messageProperties).build();
        when(timeSlotService.getTimeslotForBook(TestData.TIMESLOT_ID))
                .thenThrow(new IllegalStateException("boom"));

        rabbitTimeslotQueueListener.receiveTimeslotMessage(message);

        verify(timeSlotService).getTimeslotForBook(TestData.TIMESLOT_ID);
        verify(amqpMessageSender).sendReply(
                eq(TimeslotMessageType.BOOK_REPLY),
                eq(messageProperties),
                any(Failure.class)
        );
        assertThat(HttpStatus.INTERNAL_SERVER_ERROR).isNotNull();
    }
}
