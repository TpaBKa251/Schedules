package ru.tpu.hostel.schedules.mapper;

import org.springframework.stereotype.Component;
import ru.tpu.hostel.schedules.dto.response.ActiveEventDto;
import ru.tpu.hostel.schedules.dto.response.KitchenScheduleResponseDto;
import ru.tpu.hostel.schedules.dto.response.UserResponseDto;
import ru.tpu.hostel.schedules.dto.response.UserShortResponseDto;
import ru.tpu.hostel.schedules.entity.KitchenSchedule;

import java.util.List;

@Component
public class KitchenScheduleMapper {

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
