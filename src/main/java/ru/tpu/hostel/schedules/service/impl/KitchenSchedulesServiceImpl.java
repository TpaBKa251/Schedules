package ru.tpu.hostel.schedules.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
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

@Service
@RequiredArgsConstructor
public class KitchenSchedulesServiceImpl implements KitchenSchedulesService {

    private static final String SCHEDULE_FORBIDDEN_EXCEPTION_MESSAGE
            = "У вас нет прав редактировать расписание другого этажа";

    private static final String CONFLICT_VERSIONS_EXCEPTION_MESSAGE
            = "Кто-то уже изменил дежурство. Обновите данные и повторите попытку";

    private final KitchenSchedulesRepository kitchenSchedulesRepository;

    private final UserServiceClient userServiceClient;

    @Override
    public List<KitchenScheduleShortResponseDto> getKitchenSchedule() {
        UUID userId = ExecutionContext.get().getUserID();
        String floor = String.valueOf(userServiceClient.getRoomNumber(userId).charAt(0));
        LocalDate date = getDateForCheck();

        return kitchenSchedulesRepository.findAllOnFloorAfterDate(floor, date).stream()
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
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            backoff = @Backoff(delay = 100, multiplier = 2),
            recover = "recoverSwap"
    )
    public void swap(SwapRequestDto swapRequestDto) {
        List<KitchenSchedule> schedulesForSwap = kitchenSchedulesRepository.findDutiesForSwap(
                swapRequestDto.dutyId1(),
                swapRequestDto.dutyId2()
        );
        if (schedulesForSwap.size() != 2) {
            throw new ServiceException.NotFound("Дежурства для перестановки не найдены");
        }

        KitchenSchedule kitchenScheduleA = schedulesForSwap.get(0);
        KitchenSchedule kitchenScheduleB = schedulesForSwap.get(1);

        validateSwap(kitchenScheduleA, kitchenScheduleB);

        String tempRoom = kitchenScheduleA.getRoomNumber();
        kitchenScheduleA.setRoomNumber(kitchenScheduleB.getRoomNumber());
        kitchenScheduleB.setRoomNumber(tempRoom);

        kitchenSchedulesRepository.saveAll(List.of(kitchenScheduleA, kitchenScheduleB));
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
        char responsibleFloor = roomNumber.charAt(0);
        char roomAFloor = kitchenScheduleA.getRoomNumber().charAt(0);
        char roomBFloor = kitchenScheduleB.getRoomNumber().charAt(0);
        if (responsibleFloor != roomAFloor || responsibleFloor != roomBFloor) {
            throw new ServiceException.Forbidden(SCHEDULE_FORBIDDEN_EXCEPTION_MESSAGE);
        }
    }

    @Recover
    public void recoverSwap(ObjectOptimisticLockingFailureException e, SwapRequestDto swapRequestDto) {
        throw new ServiceException.Conflict(CONFLICT_VERSIONS_EXCEPTION_MESSAGE);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            backoff = @Backoff(delay = 100, multiplier = 2),
            recover = "recoverMarkScheduleCompleted"
    )
    public void markSchedule(UUID kitchenScheduleId) {
        KitchenSchedule kitchenSchedule = kitchenSchedulesRepository.findByIdForUpdateOptimistic(kitchenScheduleId)
                .orElseThrow(() -> new ServiceException.NotFound("Дежурство не найдено"));

        LocalDate date = getDateForCheck();
        if (kitchenSchedule.getDate().isBefore(date)) {
            throw new ServiceException.Conflict("Уже нельзя изменять отметку этого дежурства");
        }

        String roomNumber = userServiceClient.getRoomNumber(ExecutionContext.get().getUserID());
        char responsibleFloor = roomNumber.charAt(0);
        char roomFloor = kitchenSchedule.getRoomNumber().charAt(0);
        if (responsibleFloor != roomFloor) {
            throw new ServiceException.Forbidden(SCHEDULE_FORBIDDEN_EXCEPTION_MESSAGE);
        }

        kitchenSchedule.setChecked(!kitchenSchedule.getChecked());
        kitchenSchedulesRepository.save(kitchenSchedule);
    }

    @Recover
    public void recoverMarkScheduleCompleted(ObjectOptimisticLockingFailureException e, UUID kitchenScheduleId) {
        throw new ServiceException.Conflict(CONFLICT_VERSIONS_EXCEPTION_MESSAGE);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Override
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            backoff = @Backoff(delay = 100, multiplier = 2),
            recover = "recoverDeleteById"
    )
    public void deleteById(UUID id) {
        KitchenSchedule kitchenSchedule = kitchenSchedulesRepository.findByIdForUpdateOptimistic(id)
                .orElseThrow(() -> new ServiceException.NotFound("Дежурство не найдено"));

        LocalDate date = TimeUtil.now().toLocalDate();
        if (kitchenSchedule.getDate().isBefore(date)) {
            throw new ServiceException.Conflict("Уже нельзя удалить это дежурство");
        }

        String roomNumber = userServiceClient.getRoomNumber(ExecutionContext.get().getUserID());
        String floor = String.valueOf(roomNumber.charAt(0));
        if (roomNumber.charAt(0) != kitchenSchedule.getRoomNumber().charAt(0)) {
            throw new ServiceException.Forbidden(SCHEDULE_FORBIDDEN_EXCEPTION_MESSAGE);
        }

        LocalDate dateForShift = kitchenSchedule.getDate();
        kitchenSchedulesRepository.delete(kitchenSchedule);

        List<KitchenSchedule> schedulesToShift = kitchenSchedulesRepository.findAllByFloorFromDate(floor, dateForShift);

        for (KitchenSchedule schedule : schedulesToShift) {
            schedule.setDate(schedule.getDate().plusDays(1));
        }
        kitchenSchedulesRepository.saveAll(schedulesToShift);
    }

    @Recover
    public void recoverDeleteById(ObjectOptimisticLockingFailureException e, UUID id) {
        throw new ServiceException.Conflict(CONFLICT_VERSIONS_EXCEPTION_MESSAGE);
    }

    private LocalDate getDateForCheck() {
        LocalTime timeNow = TimeUtil.now().toLocalTime();
        return timeNow.isAfter(LocalTime.of(12, 0, 0))
                ? TimeUtil.now().toLocalDate()
                : TimeUtil.now().toLocalDate().minusDays(1);
    }

}
