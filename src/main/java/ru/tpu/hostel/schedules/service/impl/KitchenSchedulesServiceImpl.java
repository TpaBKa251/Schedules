package ru.tpu.hostel.schedules.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.tpu.hostel.schedules.entity.KitchenSchedule;
import ru.tpu.hostel.schedules.repository.KitchenSchedulesRepository;
import ru.tpu.hostel.schedules.service.RoomsConfig;
import ru.tpu.hostel.schedules.utils.TimeNow;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@EnableScheduling
public class KitchenSchedulesServiceImpl {

    private static final String FILE_PATH = System.getenv("ROOMS_FILE_PATH");

    private final KitchenSchedulesRepository kitchenSchedulesRepository;

    @Bean
    public ApplicationRunner checkSchedulesOnStart() {
        return args -> checkSchedules();
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Tomsk")
    public void checkSchedules() {
        for (int i = 2; i <= 5; i++) {
            LocalDate lastDate = kitchenSchedulesRepository.findLastDateOfScheduleByFloor(i).orElse(null);

            if (lastDate == null || lastDate.isEqual(TimeNow.now().toLocalDate().plusDays(2))) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());

                RoomsConfig roomsConfig;

                try {
                    roomsConfig = objectMapper.readValue(new File(FILE_PATH), RoomsConfig.class);
                } catch (IOException e) {
                    throw new RuntimeException("Не удалось загрузить список комнат", e);
                }

                List<String> rooms = roomsConfig.toMap().get(String.valueOf(i));
                List<KitchenSchedule> schedules = new ArrayList<>();

                LocalDate scheduleDate = TimeNow.now().toLocalDate().plusDays(3);

                for (String room : rooms) {
                    KitchenSchedule kitchenSchedule = new KitchenSchedule();

                    kitchenSchedule.setRoomNumber(room);
                    kitchenSchedule.setDate(scheduleDate);
                    schedules.add(kitchenSchedule);

                    scheduleDate = scheduleDate.plusDays(1);

                    kitchenSchedule = new KitchenSchedule();

                    kitchenSchedule.setRoomNumber(room);
                    kitchenSchedule.setDate(scheduleDate);
                    schedules.add(kitchenSchedule);

                    scheduleDate = scheduleDate.plusDays(1);
                }

                kitchenSchedulesRepository.saveAll(schedules);
            }
        }
    }
}
