package ru.tpu.hostel.schedules.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tpu.hostel.internal.exception.ServiceException;
import ru.tpu.hostel.internal.utils.ExecutionContext;
import ru.tpu.hostel.internal.utils.TimeUtil;
import ru.tpu.hostel.schedules.dto.request.MarkScheduleCompletedDto;
import ru.tpu.hostel.schedules.dto.request.SwapRequestDto;
import ru.tpu.hostel.schedules.dto.response.ActiveEventResponseDto;
import ru.tpu.hostel.schedules.dto.response.KitchenScheduleResponseDto;
import ru.tpu.hostel.schedules.dto.response.KitchenScheduleShortResponseDto;
import ru.tpu.hostel.schedules.entity.KitchenSchedule;
import ru.tpu.hostel.schedules.external.rest.user.UserServiceClient;
import ru.tpu.hostel.schedules.external.rest.user.dto.UserResponseDto;
import ru.tpu.hostel.schedules.mapper.KitchenScheduleMapper;
import ru.tpu.hostel.schedules.repository.KitchenSchedulesRepository;
import ru.tpu.hostel.schedules.service.KitchenSchedulesService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KitchenSchedulesServiceImpl implements KitchenSchedulesService {

    private static final String SCHEDULE_NOT_FOUND_EXCEPTION_MESSAGE = "Дежурство для комнаты %s на дату %s не найдено";

    private static final String SCHEDULE_FORBIDDEN_EXCEPTION_MESSAGE
            = "У вас нет прав редактировать расписание другого этажа";

    private final KitchenSchedulesRepository kitchenSchedulesRepository;

    private final UserServiceClient userServiceClient;

    @Override
    public List<KitchenScheduleShortResponseDto> getKitchenSchedule() {
        UUID userId = ExecutionContext.get().getUserID();
        String floor = String.valueOf(userServiceClient.getRoomNumber(userId).charAt(0));

        return kitchenSchedulesRepository.findAllOnFloor(floor).stream()
                .map(KitchenScheduleMapper::mapToKitchenScheduleShortResponseDto)
                .toList();
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
    public KitchenScheduleResponseDto getKitchenScheduleOnDate(LocalDate date) {
        UUID userId = ExecutionContext.get().getUserID();
        String roomNumber = userServiceClient.getRoomNumber(userId);
        String floor = String.valueOf(roomNumber.charAt(0));

        KitchenSchedule kitchenSchedule = kitchenSchedulesRepository.findByRoomNumberAndDate(floor, date)
                .orElseThrow(() -> new ServiceException.NotFound("Расписания на указанную дату не найдено"));

        List<UserResponseDto> usersInRoom = userServiceClient.getAllInRooms(new String[]{kitchenSchedule.getRoomNumber()});

        return KitchenScheduleMapper.mapToKitchenScheduleResponseDto(
                kitchenSchedule,
                usersInRoom
        );
    }

    @Override
    public KitchenScheduleResponseDto getKitchenScheduleById(UUID id) {
        KitchenSchedule kitchenSchedule = kitchenSchedulesRepository.findById(id)
                .orElseThrow(() -> new ServiceException.NotFound("Расписание не найдено"));
        List<UserResponseDto> users = userServiceClient.getAllInRooms(new String[]{kitchenSchedule.getRoomNumber()});

        return KitchenScheduleMapper.mapToKitchenScheduleResponseDto(kitchenSchedule, users);
    }

    @Override
    @Transactional
    public void swap(SwapRequestDto swapRequestDto) {
        ExecutionContext context = ExecutionContext.get();
        String roomNumber = userServiceClient.getRoomNumber(context.getUserID());

        char responsibleFloor = roomNumber.charAt(0);
        char roomAFloor = swapRequestDto.roomNumberA().charAt(0);
        char roomBFloor = swapRequestDto.roomNumberB().charAt(0);

        if (responsibleFloor != roomAFloor || responsibleFloor != roomBFloor) {
            throw new ServiceException.Forbidden(SCHEDULE_FORBIDDEN_EXCEPTION_MESSAGE);
        }

        KitchenSchedule scheduleA = kitchenSchedulesRepository.getScheduleForUpdate(
                        String.valueOf(roomAFloor), swapRequestDto.dateA())
                .orElseThrow(() -> new ServiceException.NotFound(
                        String.format(SCHEDULE_NOT_FOUND_EXCEPTION_MESSAGE,
                                swapRequestDto.roomNumberA(), swapRequestDto.dateA())));

        KitchenSchedule scheduleB = kitchenSchedulesRepository.getScheduleForUpdate(
                        String.valueOf(roomBFloor), swapRequestDto.dateB())
                .orElseThrow(() -> new ServiceException.NotFound(
                        String.format(SCHEDULE_NOT_FOUND_EXCEPTION_MESSAGE,
                                swapRequestDto.roomNumberB(), swapRequestDto.dateB())));

        if (!scheduleA.getScheduleNumber().equals(scheduleB.getScheduleNumber())) {
            throw new ServiceException.BadRequest("Вы не можете переставлять местами дежурства из разных расписаний");
        }

        String tempRoom = scheduleA.getRoomNumber();
        scheduleA.setRoomNumber(scheduleB.getRoomNumber());
        scheduleB.setRoomNumber(tempRoom);

        kitchenSchedulesRepository.save(scheduleA);
        kitchenSchedulesRepository.save(scheduleB);
    }

    @Override
    @Transactional
    public void markScheduleCompleted(MarkScheduleCompletedDto markScheduleCompletedDto) {
        ExecutionContext context = ExecutionContext.get();
        String roomNumber = userServiceClient.getRoomNumber(context.getUserID());

        if (roomNumber.charAt(0) != markScheduleCompletedDto.roomNumber().charAt(0)) {
            throw new ServiceException.Forbidden(SCHEDULE_FORBIDDEN_EXCEPTION_MESSAGE);
        }

        KitchenSchedule schedule = kitchenSchedulesRepository.getScheduleForUpdate(
                        String.valueOf(markScheduleCompletedDto.roomNumber().charAt(0)), markScheduleCompletedDto.date())
                .orElseThrow(() -> new ServiceException.NotFound(
                        String.format(SCHEDULE_NOT_FOUND_EXCEPTION_MESSAGE,
                                markScheduleCompletedDto.roomNumber(), markScheduleCompletedDto.date())));

        schedule.setChecked(markScheduleCompletedDto.completed());
        kitchenSchedulesRepository.save(schedule);
    }
}
