package ru.tpu.hostel.schedules.service.editor.configs.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.tpu.hostel.schedules.config.schedule.TimeslotSchedulesConfig;
import ru.tpu.hostel.schedules.dto.request.ChangeSchedulesRequestDto;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.service.editor.configs.SchedulesEditorService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SchedulesEditorServiceImpl implements SchedulesEditorService {
    /**
     * Файл-конфиг расписания для слотов
     */
    @Value("${schedules.timeslots.path}")
    private String schedulesFilePath;

    private static final String SCHEDULE_MAPPING_ERROR_LOG_MESSAGE
            = "Ошибка загрузки шаблона расписаний. Редактирование невозможно";

    private static final String SCHEDULE_MAPPING_TO_FILE_ERROR_LOG_MESSAGE
            = "Ошибка загрузки шаблона расписаний в файл. Редактирование невозможно";

    @Override
    public void editSchedule(ChangeSchedulesRequestDto changeSchedulesRequestDto, EventType editedScheduleEventType) {

        TimeslotSchedulesConfig config;

        try {
            config = TimeslotSchedulesConfig.loadFromFile(schedulesFilePath);
            if (config == null) {
                throw new IOException();
            }
        } catch (IOException e) {
            log.error(SCHEDULE_MAPPING_ERROR_LOG_MESSAGE, e);
            return;
        }

        Map<String, TimeslotSchedulesConfig.Schedule> newSchedules
                = getEditedSchedulesMap(changeSchedulesRequestDto, editedScheduleEventType, config);

        config.setSchedules(newSchedules);

        try {
            TimeslotSchedulesConfig.loadToFile(schedulesFilePath, config);
        } catch (IOException e) {
            log.error(SCHEDULE_MAPPING_TO_FILE_ERROR_LOG_MESSAGE, e);
        }
    }

    private Map<String, TimeslotSchedulesConfig.Schedule> getEditedSchedulesMap(
            ChangeSchedulesRequestDto changeSchedulesRequestDto,
            EventType editedScheduleEventType,
            TimeslotSchedulesConfig config
    ) {
        Map<String, TimeslotSchedulesConfig.Schedule> newSchedules = new HashMap<>();

        for (TimeslotSchedulesConfig.Schedule schedule : config.getSchedules().values()) {

            TimeslotSchedulesConfig.Schedule editedSchedule = editConcreteSchedule(
                    changeSchedulesRequestDto,
                    schedule,
                    editedScheduleEventType
            );

            newSchedules.put(editedSchedule.getType().toLowerCase(), editedSchedule);
        }
        return newSchedules;
    }

    private TimeslotSchedulesConfig.Schedule editConcreteSchedule(
            ChangeSchedulesRequestDto changeSchedulesRequestDto,
            TimeslotSchedulesConfig.Schedule schedule,
            EventType editedScheduleEventType
    ) {
        if (!schedule.getType().equals(editedScheduleEventType.toString()))
        {
            return schedule;
        }

        schedule.setLimit(changeSchedulesRequestDto.limit());
        schedule.setResponsible(changeSchedulesRequestDto.responsible());
        schedule.setWorkingDays(changeSchedulesRequestDto.workingDays());
        schedule.setWorkingHours(changeSchedulesRequestDto.workingHours());
        schedule.setSlotDurationMinutes(changeSchedulesRequestDto.slotDurationMinutes());
        schedule.setBreaks(changeSchedulesRequestDto.breaks());
        schedule.setReservedHours(changeSchedulesRequestDto.reservedHours());

        return schedule;
    }
}
