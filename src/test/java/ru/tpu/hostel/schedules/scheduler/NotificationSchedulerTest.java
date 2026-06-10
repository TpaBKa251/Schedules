package ru.tpu.hostel.schedules.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tpu.hostel.schedules.TestData;
import ru.tpu.hostel.schedules.repository.KitchenSchedulesRepository;
import ru.tpu.hostel.schedules.external.rest.user.UserServiceClient;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulerTest {

    @Mock
    private KitchenSchedulesRepository kitchenSchedulesRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private NotificationScheduler notificationScheduler;

    @Test
    void sendNotificationWithSuccess() {
        when(kitchenSchedulesRepository.findAllByDateEquals(any(LocalDate.class)))
                .thenReturn(List.of(TestData.defaultKitchenSchedule()));
        when(userServiceClient.getAllInRoomsWithId(any(String[].class)))
                .thenReturn(List.of(TestData.USER_ID));

        notificationScheduler.sendNotification();

        verify(kitchenSchedulesRepository, times(2)).findAllByDateEquals(any(LocalDate.class));
        verify(userServiceClient, times(2)).getAllInRoomsWithId(any(String[].class));
    }
}
