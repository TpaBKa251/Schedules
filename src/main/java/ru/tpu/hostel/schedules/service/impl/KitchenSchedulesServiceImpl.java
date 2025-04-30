package ru.tpu.hostel.schedules.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tpu.hostel.schedules.client.UserServiceClient;
import ru.tpu.hostel.schedules.dto.request.MarkScheduleCompletedDto;
import ru.tpu.hostel.schedules.dto.request.SwapRequestDto;
import ru.tpu.hostel.schedules.dto.response.KitchenScheduleResponseDto;
import ru.tpu.hostel.schedules.dto.response.UserResponseDto;
import ru.tpu.hostel.schedules.entity.KitchenSchedule;
import ru.tpu.hostel.schedules.mapper.KitchenScheduleMapper;
import ru.tpu.hostel.schedules.repository.KitchenSchedulesRepository;
import ru.tpu.hostel.schedules.service.KitchenSchedulesService;
import ru.tpu.hostel.schedules.service.RoomsConfig;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KitchenSchedulesServiceImpl implements KitchenSchedulesService {

    @Value("${schedules.kitchen.path}")
    private String filePath;

    private final KitchenSchedulesRepository kitchenSchedulesRepository;

    private final UserServiceClient userServiceClient;

    @Bean
    public ApplicationRunner checkSchedulesOnStart() {
        return args -> checkSchedules();
    }

    @Scheduled(cron = "0 0 12 * * *")
    @jakarta.transaction.Transactional
    public void handleMissedSchedules() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        List<KitchenSchedule> missedSchedules = kitchenSchedulesRepository
                .findAllByDateAndChecked(yesterday, false);

        for (KitchenSchedule missedSchedule : missedSchedules) {
            String roomNumber = missedSchedule.getRoomNumber();
            String floor = String.valueOf(roomNumber.charAt(0));

            // Переносим все дежурства, включая пропущенное, на следующий день
            shiftFloorSchedules(floor, yesterday);

        }
    }

    private void shiftFloorSchedules(String floor, LocalDate fromDate) {
        List<KitchenSchedule> schedulesToShift = kitchenSchedulesRepository
                .findAllByFloorFromDate(floor, fromDate);

        for (KitchenSchedule schedule : schedulesToShift) {
            schedule.setDate(schedule.getDate().plusDays(1));
        }
        kitchenSchedulesRepository.saveAll(schedulesToShift);
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Tomsk")
    public void checkSchedules() {
        for (int i = 2; i <= 5; i++) {
            LocalDate lastDate = kitchenSchedulesRepository.findLastDateOfScheduleByFloor(i).orElse(null);
            Integer lastNumber = kitchenSchedulesRepository.findLastNumberOfScheduleByFloor(i).orElse(null);

            if (lastDate == null || lastDate.isEqual(TimeUtil.now().toLocalDate().plusDays(2))) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());

                RoomsConfig roomsConfig;

                try {
                    roomsConfig = objectMapper.readValue(new File(filePath), RoomsConfig.class);
                } catch (IOException e) {
                    throw new RuntimeException("Не удалось загрузить список комнат", e);
                }

                List<String> rooms = roomsConfig.toMap().get(String.valueOf(i));
                List<KitchenSchedule> schedules = new ArrayList<>();

                LocalDate scheduleDate = lastDate == null
                        ? TimeUtil.now().toLocalDate()
                        : TimeUtil.now().toLocalDate().plusDays(3);

                for (String room : rooms) {
                    KitchenSchedule kitchenSchedule = new KitchenSchedule();

                    kitchenSchedule.setRoomNumber(room);
                    kitchenSchedule.setDate(scheduleDate);
                    kitchenSchedule.setScheduleNumber(lastNumber == null ? 1 : lastNumber + 1);
                    kitchenSchedule.setChecked(false);
                    schedules.add(kitchenSchedule);

                    scheduleDate = scheduleDate.plusDays(1);
                }

                kitchenSchedulesRepository.saveAll(schedules);
            }

            LocalDate date = kitchenSchedulesRepository.findDateByRoomNumber("202").orElse(null);

            if (date != null && TimeUtil.now().toLocalDate().equals(date)) {
                kitchenSchedulesRepository.deleteAllByDateLessThan(date.minusDays(2));
            }
        }
    }

    @Override
    public List<KitchenScheduleResponseDto> getKitchenSchedule() {
        UUID userId = ExecutionContext.get().getUserID();
        String floor = String.valueOf(userServiceClient.getRoomNumber(userId).charAt(0));

        Set<String> rooms = kitchenSchedulesRepository.findAllRoomsOnFloor(floor);

        List<UserResponseDto> users = userServiceClient.getAllInRooms(rooms.toArray(new String[0]));

        List<KitchenSchedule> kitchenSchedules = kitchenSchedulesRepository.findAllOnFloor(floor);

        List<KitchenScheduleShortResponseDto> kitchenScheduleShortResponseDtos = new ArrayList<>();

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

            kitchenScheduleShortResponseDtos.add(
                    KitchenScheduleMapper.mapToKitchenScheduleShortResponseDto(kitchenSchedule)
            );

            KitchenSchedule kitchenSchedule2 = iterator.next();

            kitchenScheduleShortResponseDtos.add(
                    KitchenScheduleMapper.mapToKitchenScheduleShortResponseDto(kitchenSchedule2)
            );
        }

        return kitchenScheduleShortResponseDtos;
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
                .orElseThrow(RuntimeException::new);

        List<UserResponseDto> usersInRoom = userServiceClient.getAllInRooms(new String[]{kitchenSchedule.getRoomNumber()});

        return KitchenScheduleMapper.mapToKitchenScheduleResponseDto(
                kitchenSchedule,
                usersInRoom
        );
    }

    @Override
    public KitchenScheduleResponseDto getKitchenScheduleById(UUID userId) {
        KitchenSchedule kitchenSchedule = kitchenSchedulesRepository.findKitchenScheduleById(userId).orElseThrow(EntityNotFoundException::new);
        List<UserResponseDto> users = userServiceClient.getAllInRooms(new String[]{kitchenSchedule.getRoomNumber()});

        return KitchenScheduleMapper.mapToKitchenScheduleResponseDto(kitchenSchedule, users);
    }

    @Override
    @Transactional
    public void swap(SwapRequestDto swapRequestDto) {
        // Проверяем, что комнаты на одном этаже
        if (swapRequestDto.roomNumberA().charAt(0) != swapRequestDto.roomNumberB().charAt(0)) {
            throw new IllegalArgumentException("Комнаты должны быть на одном этаже");
        }

        KitchenSchedule scheduleA = kitchenSchedulesRepository.findByRoomNumberAndDate(
                        String.valueOf(swapRequestDto.roomNumberA().charAt(0)), swapRequestDto.dateA())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Дежурство для комнаты %s на дату %s не найдено",
                                swapRequestDto.roomNumberA(), swapRequestDto.dateA())));

        KitchenSchedule scheduleB = kitchenSchedulesRepository.findByRoomNumberAndDate(
                        String.valueOf(swapRequestDto.roomNumberB().charAt(0)), swapRequestDto.dateB())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Дежурство для комнаты %s на дату %s не найдено",
                                swapRequestDto.roomNumberB(), swapRequestDto.dateB())));

        // Меняем комнаты местами
        String tempRoom = scheduleA.getRoomNumber();
        scheduleA.setRoomNumber(scheduleB.getRoomNumber());
        scheduleB.setRoomNumber(tempRoom);

        kitchenSchedulesRepository.save(scheduleA);
        kitchenSchedulesRepository.save(scheduleB);
    }

    @Override
    @Transactional
    public void markScheduleCompleted(MarkScheduleCompletedDto markScheduleCompletedDto) {
        KitchenSchedule schedule = kitchenSchedulesRepository.findByRoomNumberAndDate(
                        String.valueOf(markScheduleCompletedDto.roomNumber().charAt(0)), markScheduleCompletedDto.date())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Дежурство для комнаты %s на дату %s не найдено",
                                markScheduleCompletedDto.roomNumber(), markScheduleCompletedDto.date())));

        schedule.setChecked(markScheduleCompletedDto.completed());
        kitchenSchedulesRepository.save(schedule);
    }
}
