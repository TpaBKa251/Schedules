package ru.tpu.hostel.schedules.service.impl;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.tpu.hostel.internal.exception.ServiceException;
import ru.tpu.hostel.internal.utils.ExecutionContext;
import ru.tpu.hostel.internal.utils.TimeUtil;
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
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

// TODO Добавить кэширование комнат или этажей юзеров
@Slf4j
@Service
@RequiredArgsConstructor
public class KitchenSchedulesServiceImpl implements KitchenSchedulesService {

    private static final String FORBIDDEN_EXCEPTION_MESSAGE = "У вас нет прав редактировать расписание другого этажа";

    private static final String CONFLICT_VERSIONS_EXCEPTION_MESSAGE
            = "Кто-то уже изменил дежурство. Обновите данные и повторите попытку";

    private static final String DUTY_NOT_FOUND_EXCEPTION_MESSAGE = "Дежурство не найдено";

    private static final int WEEK = 7;

    private static final int FLOOR_INDEX = 0;

    private final KitchenSchedulesRepository kitchenSchedulesRepository;

    private final UserServiceClient userServiceClient;

    @Override
    public List<KitchenScheduleShortResponseDto> getSchedule(String floorFromRequest) {
        LocalDate date = getDateForCheck();
        return kitchenSchedulesRepository.findAllOnFloorAfterDate(floorFromRequest, date).stream()
                .map(KitchenScheduleMapper::mapToKitchenScheduleShortResponseDto)
                .toList();
    }

    @Override
    public List<KitchenScheduleShortResponseDto> getSchedule() {
        UUID userId = ExecutionContext.get().getUserID();
        LocalDate date = getDateForCheck();
        try {
            String userFloor = String.valueOf(userServiceClient.getRoomNumber(userId).charAt(FLOOR_INDEX));
            return getSchedule(userFloor);
        } catch (FeignException e) {
            if (e.status() >= 400 && e.status() < 500) {
                throw e;
            }
            log.warn("User-service не ответил, отправляю расписание на все этажи");
            return kitchenSchedulesRepository.findAllAfterDate(date).stream()
                    .map(KitchenScheduleMapper::mapToKitchenScheduleShortResponseDto)
                    .toList();
        }
    }

    @Override
    public List<ActiveEventResponseDto> getActiveDuties(String roomNumber) {
        LocalDate today = TimeUtil.now().toLocalDate();
        return kitchenSchedulesRepository
                .findAllActiveDuties(roomNumber, today.plusDays(WEEK), today)
                .stream()
                .map(KitchenScheduleMapper::mapToActiveEventDto)
                .toList();
    }

    @Override
    public List<ActiveEventResponseDto> getActiveDuties(UUID userId) {
        String roomNumber = userServiceClient.getRoomNumber(userId);
        return getActiveDuties(roomNumber);
    }

    @Override
    public KitchenScheduleResponseDto getDutyOnDate(LocalDate date, String floorFromRequest) {
        UUID userId = ExecutionContext.get().getUserID();
        String floor = floorFromRequest == null || floorFromRequest.isEmpty()
                ? String.valueOf(userServiceClient.getRoomNumber(userId).charAt(FLOOR_INDEX))
                : floorFromRequest;

        KitchenSchedule kitchenSchedule = kitchenSchedulesRepository.findByFloorAndDate(floor, date)
                .orElseThrow(() -> new ServiceException.NotFound(DUTY_NOT_FOUND_EXCEPTION_MESSAGE));

        List<UserResponseDto> usersInRoom = userServiceClient.getAllInRooms(
                new String[]{kitchenSchedule.getRoomNumber()}
        );

        return KitchenScheduleMapper.mapToKitchenScheduleResponseDto(
                kitchenSchedule,
                usersInRoom
        );
    }

    @Override
    public KitchenScheduleResponseDto getDutyById(UUID id) {
        KitchenSchedule kitchenSchedule = kitchenSchedulesRepository.findById(id)
                .orElseThrow(() -> new ServiceException.NotFound(DUTY_NOT_FOUND_EXCEPTION_MESSAGE));
        List<UserResponseDto> users = List.of();
        try {
            users = userServiceClient.getAllInRooms(new String[]{kitchenSchedule.getRoomNumber()});
        } catch (FeignException e) {
            if (e.status() >= 400 && e.status() < 500) {
                throw e;
            }
            log.warn("User-service не ответил, отправляю дежурство без юзеров");
        }

        return KitchenScheduleMapper.mapToKitchenScheduleResponseDto(kitchenSchedule, users);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void swapDuties(SwapRequestDto swapRequestDto) {
        List<KitchenSchedule> schedulesForSwap = kitchenSchedulesRepository.findDutiesForSwap(
                swapRequestDto.dutyId1(),
                swapRequestDto.dutyId2()
        );
        if (schedulesForSwap.size() != 2) {
            throw new ServiceException.NotFound(DUTY_NOT_FOUND_EXCEPTION_MESSAGE);
        }

        KitchenSchedule kitchenScheduleA = schedulesForSwap.get(0);
        KitchenSchedule kitchenScheduleB = schedulesForSwap.get(1);

        validateSwap(kitchenScheduleA, kitchenScheduleB);

        String tempRoom = kitchenScheduleA.getRoomNumber();
        kitchenScheduleA.setRoomNumber(kitchenScheduleB.getRoomNumber());
        kitchenScheduleB.setRoomNumber(tempRoom);

        try {
            kitchenSchedulesRepository.saveAll(List.of(kitchenScheduleA, kitchenScheduleB));
            kitchenSchedulesRepository.flush();
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ServiceException.Conflict(CONFLICT_VERSIONS_EXCEPTION_MESSAGE);
        }
    }

    private void validateSwap(KitchenSchedule kitchenScheduleA, KitchenSchedule kitchenScheduleB) {
        LocalDate today = TimeUtil.now().toLocalDate();
        if (kitchenScheduleA.getDate().isBefore(today) || kitchenScheduleB.getDate().isBefore(today)) {
            throw new ServiceException.Conflict("Вы не можете переставлять прошедшие дежурства");
        }

        // TODO Подумать, нужна ли такая проверка
//        if (!kitchenScheduleA.getScheduleNumber().equals(kitchenScheduleB.getScheduleNumber())) {
//            throw new ServiceException.BadRequest("Вы не можете переставлять местами дежурства из разных расписаний");
//        }

        String roomNumber = userServiceClient.getRoomNumber(ExecutionContext.get().getUserID());
        char responsibleFloor = roomNumber.charAt(FLOOR_INDEX);
        char roomAFloor = kitchenScheduleA.getRoomNumber().charAt(FLOOR_INDEX);
        char roomBFloor = kitchenScheduleB.getRoomNumber().charAt(FLOOR_INDEX);
        if (responsibleFloor != roomAFloor || responsibleFloor != roomBFloor) {
            throw new ServiceException.Forbidden(FORBIDDEN_EXCEPTION_MESSAGE);
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void markDuty(UUID kitchenScheduleId) {
        KitchenSchedule kitchenSchedule = kitchenSchedulesRepository.findByIdForUpdateOptimistic(kitchenScheduleId)
                .orElseThrow(() -> new ServiceException.NotFound(DUTY_NOT_FOUND_EXCEPTION_MESSAGE));

        LocalDate date = getDateForCheck();
        if (kitchenSchedule.getDate().isBefore(date)) {
            throw new ServiceException.Conflict("Уже нельзя изменять отметку этого дежурства");
        }

        String roomNumber = userServiceClient.getRoomNumber(ExecutionContext.get().getUserID());
        char responsibleFloor = roomNumber.charAt(FLOOR_INDEX);
        char roomFloor = kitchenSchedule.getRoomNumber().charAt(FLOOR_INDEX);
        if (responsibleFloor != roomFloor) {
            throw new ServiceException.Forbidden(FORBIDDEN_EXCEPTION_MESSAGE);
        }

        kitchenSchedule.setChecked(!kitchenSchedule.getChecked());
        try {
            kitchenSchedulesRepository.save(kitchenSchedule);
            kitchenSchedulesRepository.flush();
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ServiceException.Conflict(CONFLICT_VERSIONS_EXCEPTION_MESSAGE);
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Override
    public void deleteDutyById(UUID id) {
        KitchenSchedule kitchenSchedule = kitchenSchedulesRepository.findByIdForUpdateOptimistic(id)
                .orElseThrow(() -> new ServiceException.NotFound(DUTY_NOT_FOUND_EXCEPTION_MESSAGE));

        LocalDate date = TimeUtil.now().toLocalDate();
        if (kitchenSchedule.getDate().isBefore(date)) {
            throw new ServiceException.Conflict("Уже нельзя удалить это дежурство");
        }

        String roomNumber = userServiceClient.getRoomNumber(ExecutionContext.get().getUserID());
        String floor = String.valueOf(roomNumber.charAt(FLOOR_INDEX));
        if (roomNumber.charAt(FLOOR_INDEX) != kitchenSchedule.getRoomNumber().charAt(FLOOR_INDEX)) {
            throw new ServiceException.Forbidden(FORBIDDEN_EXCEPTION_MESSAGE);
        }

        LocalDate dateForShift = kitchenSchedule.getDate();
        try {
            kitchenSchedulesRepository.delete(kitchenSchedule);
            kitchenSchedulesRepository.flush();
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ServiceException.Conflict(CONFLICT_VERSIONS_EXCEPTION_MESSAGE);
        }

        List<KitchenSchedule> schedulesToShift = kitchenSchedulesRepository.findAllByFloorFromDate(floor, dateForShift);

        for (KitchenSchedule schedule : schedulesToShift) {
            schedule.setDate(schedule.getDate().plusDays(1));
        }
        kitchenSchedulesRepository.saveAll(schedulesToShift);
    }

    private LocalDate getDateForCheck() {
        LocalTime timeNow = TimeUtil.now().toLocalTime();
        return timeNow.isAfter(LocalTime.NOON)
                ? TimeUtil.now().toLocalDate()
                : TimeUtil.now().toLocalDate().minusDays(1);
    }

}
