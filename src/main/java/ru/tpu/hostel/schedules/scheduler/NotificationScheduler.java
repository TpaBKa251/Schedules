package ru.tpu.hostel.schedules.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tpu.hostel.schedules.external.rest.notification.NotificationClient;
import ru.tpu.hostel.schedules.external.rest.notification.dto.NotificationRequestDto;
import ru.tpu.hostel.schedules.external.rest.notification.dto.NotificationType;
import ru.tpu.hostel.schedules.external.rest.user.UserServiceClient;
import ru.tpu.hostel.schedules.entity.KitchenSchedule;
import ru.tpu.hostel.schedules.repository.KitchenSchedulesRepository;
import ru.tpu.hostel.schedules.config.schedule.RoomsConfig;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationScheduler {

    private static final int WEEK_IN_DAYS = 7;
    private static final int WEEK_IN_DAYS_PLUS_ONE = 8;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final int ONE_DAY = 1;
    private static final int TWO_DAYS = 2;

    private final KitchenSchedulesRepository kitchenSchedulesRepository;

    private final NotificationClient notificationClient;
    private final UserServiceClient userServiceClient;

    //@Scheduled(cron = "0 0 8 * * *", zone = "Asia/Tomsk")
    public void sendNotification() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Tomsk"));

        LocalDate dateForSchedule = today.plusDays(WEEK_IN_DAYS);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        RoomsConfig roomsConfig;

        // TODO сделать два поиска комнаты, которая дежурит 2 дня подряд на каждом этаже
        //  (поиски через неделю и через день, если в 2 дня дежурит 2 комнаты, то пропустить этаж)

        // TODO сделать НОРМАЛЬНУЮ загрузку списка этажей из конфига
        //String roomOfSchedule1 = kitchenSchedulesRepository.findRoomNumberByDateAndFloor()
        sendNotificationAboutScheduleInKitchen(dateForSchedule);

        dateForSchedule = today.plusDays(ONE_DAY);
        sendNotificationAboutScheduleInKitchen(dateForSchedule);
    }

    private void sendNotificationAboutScheduleInKitchen(LocalDate date) {
        List<KitchenSchedule> kitchenSchedules = kitchenSchedulesRepository.findAllByDateEquals(date);

        List<String> rooms = new ArrayList<>();

        for (KitchenSchedule kitchenSchedule : kitchenSchedules) {
            rooms.add(kitchenSchedule.getRoomNumber());
        }

        List<UUID> userIds = userServiceClient.getAllInRoomsWithId(rooms.toArray(new String[0]));

        for (UUID userId : userIds) {
            NotificationRequestDto notification = new NotificationRequestDto(
                    userId,
                    NotificationType.KITCHEN_SCHEDULE,
                    "Дежурство на кухне "
                            + selectDateMessage(date) + " и " + selectDateMessage(date.plusDays(ONE_DAY)),
                    "Не забудьте про дежурство на кухне: "
                    + selectDateMessage(date) + " и " + selectDateMessage(date.plusDays(ONE_DAY))
            );
        }
    }

    private void logError(UUID userId, Exception e) {
        log.error(
                "Уведомление для пользователя {} не было отправлено, но возможно было сохранено: {}",
                userId,
                e.getMessage(),
                e
        );
    }

    private String selectDateMessage(LocalDate date) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Tomsk"));
        String scheduleDate = date.format(DATE_FORMATTER);

        if (today.isEqual(date)) {
            return "сегодня";
        }
        if (today.isEqual(date.plusDays(ONE_DAY))) {
            return "завтра (" + scheduleDate + ")";
        }
        if (today.isEqual(date.plusDays(TWO_DAYS))) {
            return "послезавтра (" + scheduleDate + ")";
        }

        return scheduleDate;
    }
}
