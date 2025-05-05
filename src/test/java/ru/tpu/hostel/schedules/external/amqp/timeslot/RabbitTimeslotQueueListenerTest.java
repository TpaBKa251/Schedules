//package ru.tpu.hostel.schedules.external.amqp.timeslot;
//
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.core.MessageBuilder;
//import org.springframework.amqp.core.MessageProperties;
//import org.springframework.amqp.core.MessagePropertiesBuilder;
//import ru.tpu.hostel.schedules.dto.response.TimeslotResponse;
//import ru.tpu.hostel.schedules.service.TimeslotService;
//
//import java.io.IOException;
//import java.util.UUID;
//
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoMoreInteractions;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class RabbitTimeslotQueueListenerTest {
//
//    @Mock
//    private AmqpMessageSender timeslotAmqpMessageSender;
//
//    @Mock
//    private TimeslotService timeSlotService;
//
//    @InjectMocks
//    private RabbitTimeslotQueueListener rabbitTimeslotQueueListener;
//
//    @AfterEach
//    void commonVerify() {
//        verifyNoMoreInteractions(timeslotAmqpMessageSender, timeSlotService);
//    }
//
//    @Test
//    void receiveTimeslotMessageWithSuccess() throws IOException {
//        MessageProperties messageProperties = MessagePropertiesBuilder.newInstance().build();
//        UUID timeslotId = UUID.randomUUID();
//        String messageBody = "\"" + timeslotId + "\"";
//        Message message = MessageBuilder
//                .withBody(messageBody.getBytes())
//                .andProperties(messageProperties)
//                .build();
//        TimeslotResponse timeSlotResponse = new TimeslotResponse(timeslotId, "10:00-12:00");
//
//        when(timeSlotService.getTimeSlotById(timeslotId)).thenReturn(timeSlotResponse);
//
//        rabbitTimeslotQueueListener.receiveTimeslotMessage(message);
//
//        verify(timeslotAmqpMessageSender).sendReply(messageProperties, timeSlotResponse);
//    }
//
//    @Test
//    void receiveTimeslotMessageWithThrowingExceptionOnIdMapping() throws IOException {
//        MessageProperties messageProperties = MessagePropertiesBuilder.newInstance().build();
//        Message message = MessageBuilder
//                .withBody("".getBytes())
//                .andProperties(messageProperties)
//                .build();
//
//        rabbitTimeslotQueueListener.receiveTimeslotMessage(message);
//
//        verify(timeslotAmqpMessageSender).sendReply(messageProperties, null);
//    }
//
//    @Test
//    void receiveTimeslotMessageWhenTimeslotServiceReturnsNull() throws IOException {
//        MessageProperties messageProperties = MessagePropertiesBuilder.newInstance().build();
//        UUID timeslotId = UUID.randomUUID();
//        String messageBody = "\"" + timeslotId + "\"";
//        Message message = MessageBuilder
//                .withBody(messageBody.getBytes())
//                .andProperties(messageProperties)
//                .build();
//
//        when(timeSlotService.getTimeSlotById(timeslotId)).thenReturn(null);
//
//        rabbitTimeslotQueueListener.receiveTimeslotMessage(message);
//
//        verify(timeslotAmqpMessageSender).sendReply(messageProperties, null);
//    }
//
//}