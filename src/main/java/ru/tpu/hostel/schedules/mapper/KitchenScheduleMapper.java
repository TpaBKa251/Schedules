package ru.tpu.hostel.schedules.mapper;

import org.springframework.stereotype.Component;
import ru.tpu.hostel.schedules.dto.response.*;
import ru.tpu.hostel.schedules.entity.KitchenSchedule;

import java.util.List;

@Component
public class KitchenScheduleMapper {

    public static KitchenScheduleShortResponseDto mapToKitchenScheduleShortResponseDto(
            KitchenSchedule kitchenSchedule
    ) {
        return new KitchenScheduleShortResponseDto(
                kitchenSchedule.getId(),
                kitchenSchedule.getDate(),
                kitchenSchedule.getRoomNumber(),
                kitchenSchedule.getChecked()
        );
    }

    public static KitchenScheduleResponseDto mapToKitchenScheduleResponseDto(
            KitchenSchedule kitchenSchedule,
            List<UserResponseDto> users
    ) {
        return new KitchenScheduleResponseDto(
                kitchenSchedule.getId(),
                kitchenSchedule.getDate(),
                kitchenSchedule.getRoomNumber(),
                kitchenSchedule.getChecked(),
                users.stream()
                        .map(u -> new UserShortResponseDto(u.firstName(), u.lastName(), u.middleName()))
                        .toList()
        );
    }

    public static ActiveEventDto mapToActiveEventDto(KitchenSchedule kitchenSchedule) {
        return new ActiveEventDto(
                kitchenSchedule.getId(),
                kitchenSchedule.getDate().atTime(0, 0),
                kitchenSchedule.getDate().atTime(23, 59),
                "BOOKED",
                "Дежурство на кухне"
        );
    }
}
