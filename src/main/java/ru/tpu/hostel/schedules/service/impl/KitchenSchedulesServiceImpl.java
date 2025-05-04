package ru.tpu.hostel.schedules.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.tpu.hostel.internal.utils.TimeUtil;
import ru.tpu.hostel.schedules.client.UserServiceClient;
import ru.tpu.hostel.schedules.dto.response.ActiveEventDto;
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
@EnableScheduling
public class KitchenSchedulesServiceImpl implements KitchenSchedulesService {

    @Value("${schedules.kitchen.path}")
    private String filePath;

    private final KitchenSchedulesRepository kitchenSchedulesRepository;

    private final UserServiceClient userServiceClient;

    @Bean
    public ApplicationRunner checkSchedulesOnStart() {
        return args -> checkSchedules();
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

                    kitchenSchedule = new KitchenSchedule();

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
    public List<ActiveEventDto> getActiveEvent(UUID userId) {
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
