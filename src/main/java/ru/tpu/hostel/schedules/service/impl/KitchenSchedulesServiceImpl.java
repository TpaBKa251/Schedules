package ru.tpu.hostel.schedules.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.tpu.hostel.internal.utils.TimeUtil;
import ru.tpu.hostel.schedules.dto.response.ActiveEventResponseDto;
import ru.tpu.hostel.schedules.dto.response.KitchenScheduleResponseDto;
import ru.tpu.hostel.schedules.entity.KitchenSchedule;
import ru.tpu.hostel.schedules.external.rest.user.UserServiceClient;
import ru.tpu.hostel.schedules.external.rest.user.dto.UserResponseDto;
import ru.tpu.hostel.schedules.mapper.KitchenScheduleMapper;
import ru.tpu.hostel.schedules.repository.KitchenSchedulesRepository;
import ru.tpu.hostel.schedules.service.KitchenSchedulesService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KitchenSchedulesServiceImpl implements KitchenSchedulesService {

    private final KitchenSchedulesRepository kitchenSchedulesRepository;

    private final UserServiceClient userServiceClient;

    @Override
    public List<KitchenScheduleResponseDto> getKitchenSchedule(UUID userId, int page, int size) {
        String floor = String.valueOf(userServiceClient.getRoomNumber(userId).charAt(0));

        Pageable pageable = PageRequest.of(page, size);
        Set<String> rooms = kitchenSchedulesRepository.findAllRoomsOnFloor(floor, pageable).toSet();

        List<UserResponseDto> users = userServiceClient.getAllInRooms(rooms.toArray(new String[0]));

        List<KitchenSchedule> kitchenSchedules = kitchenSchedulesRepository.findAllOnFloor(floor, pageable).toList();

        List<KitchenScheduleResponseDto> kitchenScheduleResponseDtos = new ArrayList<>();

        Iterator<KitchenSchedule> iterator = kitchenSchedules.iterator();

        while (iterator.hasNext()) {
            KitchenSchedule kitchenSchedule = iterator.next();

            List<UserResponseDto> usersInRoom = new ArrayList<>();

            for (int i = 0; i < users.size(); i++) {
                if (!users.get(i).roomNumber().equals(kitchenSchedule.getRoomNumber())) {
                    users.removeAll(usersInRoom);
                    break;
                }

                usersInRoom.add(users.get(i));
            }

            kitchenScheduleResponseDtos.add(
                    KitchenScheduleMapper.mapToKitchenScheduleResponseDto(kitchenSchedule, usersInRoom)
            );

            KitchenSchedule kitchenSchedule2 = iterator.next();

            kitchenScheduleResponseDtos.add(
                    KitchenScheduleMapper.mapToKitchenScheduleResponseDto(kitchenSchedule2, usersInRoom)
            );
        }

        return kitchenScheduleResponseDtos;
    }

    @Override
    public List<ActiveEventResponseDto> getActiveEvent(UUID userId) {
        String roomNumber = userServiceClient.getRoomNumber(userId);

        return kitchenSchedulesRepository
                .findAllByRoomNumberAndDateLessThanEqual(roomNumber, TimeUtil.now().toLocalDate().plusDays(7))
                .stream()
                .map(KitchenScheduleMapper::mapToActiveEventDto)
                .toList();
    }

    @Override
    public KitchenScheduleResponseDto getKitchenScheduleOnDate(LocalDate date, UUID userId) {
        String roomNumber = userServiceClient.getRoomNumber(userId);
        String floor = String.valueOf(roomNumber.charAt(0));

        KitchenSchedule kitchenSchedule = kitchenSchedulesRepository.findByRoomNumberAndDate(floor, date)
                .orElseThrow(RuntimeException::new);

        List<UserResponseDto> usersInRoom = userServiceClient.getAllInRooms(new String[]{kitchenSchedule.getRoomNumber()});

        return KitchenScheduleMapper.mapToKitchenScheduleResponseDto(
                kitchenSchedule,
                usersInRoom
        );
    }
}
