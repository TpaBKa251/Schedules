package ru.tpu.hostel.schedules.mapper;

import lombok.experimental.UtilityClass;
import ru.tpu.hostel.internal.utils.TimeUtil;
import ru.tpu.hostel.schedules.dto.response.ActiveEventResponseDto;
import ru.tpu.hostel.schedules.dto.response.KitchenScheduleResponseDto;
import ru.tpu.hostel.schedules.dto.response.KitchenScheduleShortResponseDto;
import ru.tpu.hostel.schedules.dto.response.UserShortResponseDto;
import ru.tpu.hostel.schedules.entity.KitchenSchedule;
import ru.tpu.hostel.schedules.external.rest.user.dto.UserResponseDto;

import java.util.List;

@UtilityClass
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

    public static ActiveEventResponseDto mapToActiveEventDto(KitchenSchedule kitchenSchedule) {
        return new ActiveEventResponseDto(
                kitchenSchedule.getId(),
                kitchenSchedule.getDate().atTime(0, 0),
                kitchenSchedule.getDate().atTime(23, 59),
                TimeUtil.now().toLocalDate().equals(kitchenSchedule.getDate()) ? "IN_PROGRESS" : "BOOKED",
                "Дежурство на кухне"
        );
    }
}
