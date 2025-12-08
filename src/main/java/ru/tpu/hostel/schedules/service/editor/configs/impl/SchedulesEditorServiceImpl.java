package ru.tpu.hostel.schedules.service.editor.configs.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.tpu.hostel.internal.exception.ServiceException;
import ru.tpu.hostel.schedules.config.schedule.TimeslotSchedulesConfig;
import ru.tpu.hostel.schedules.dto.SchedulesDto;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.mapper.ScheduleMapper;
import ru.tpu.hostel.schedules.service.editor.configs.SchedulesEditorService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SchedulesEditorServiceImpl implements SchedulesEditorService {

    private static final String SCHEDULE_MAPPING_ERROR_LOG_MESSAGE
            = "Ошибка загрузки шаблона расписаний из файла. Редактирование невозможно";

    private static final String SCHEDULE_MAPPING_TO_FILE_ERROR_LOG_MESSAGE
            = "Ошибка загрузки шаблона расписаний в файл. Редактирование невозможно";

    private final ScheduleMapper scheduleMapper;

    /**
     * Файл-конфиг расписания для слотов
     */
    @Value("${schedules.timeslots.path}")
    private String schedulesFilePath;

    @Override
    public void editSchedule(SchedulesDto schedulesDto, EventType editedScheduleEventType) {

        TimeslotSchedulesConfig config = getTimeSlotSchedulesConfig();

        Map<String, TimeslotSchedulesConfig.Schedule> newSchedules
                = getEditedSchedulesMap(schedulesDto, editedScheduleEventType, config);

        config.setSchedules(newSchedules);

        try {
            TimeslotSchedulesConfig.loadToFile(schedulesFilePath, config);
        } catch (IOException e) {
            throw new ServiceException.InternalServerError(SCHEDULE_MAPPING_TO_FILE_ERROR_LOG_MESSAGE);
        }
    }

    @Override
    public SchedulesDto getSchedule(EventType eventType) {

        Map<String, TimeslotSchedulesConfig.Schedule> schedules = getTimeSlotSchedulesConfig().getSchedules();

        return scheduleMapper.mapToSchedulesDto(schedules.get(eventType.name().toLowerCase()));
    }

    private Map<String, TimeslotSchedulesConfig.Schedule> getEditedSchedulesMap(
            SchedulesDto schedulesDto,
            EventType editedScheduleEventType,
            TimeslotSchedulesConfig config
    ) {
        Map<String, TimeslotSchedulesConfig.Schedule> newSchedules = new HashMap<>();

        for (TimeslotSchedulesConfig.Schedule schedule : config.getSchedules().values()) {

            TimeslotSchedulesConfig.Schedule editedSchedule = editConcreteSchedule(
                    schedulesDto,
                    schedule,
                    editedScheduleEventType
            );

            newSchedules.put(editedSchedule.getType().toLowerCase(), editedSchedule);
        }
        return newSchedules;
    }

    private TimeslotSchedulesConfig.Schedule editConcreteSchedule(
            SchedulesDto schedulesDto,
            TimeslotSchedulesConfig.Schedule schedule,
            EventType editedScheduleEventType
    ) {
        if (!schedule.getType().equalsIgnoreCase(editedScheduleEventType.toString())) {
            return schedule;
        }

        schedule.setLimit(schedulesDto.limit());
        schedule.setResponsible(schedulesDto.responsible());
        schedule.setWorkingDays(schedulesDto.workingDays());
        schedule.setWorkingHours(schedulesDto.workingHours());
        schedule.setSlotDurationMinutes(schedulesDto.slotDurationMinutes());
        schedule.setBreaks(schedulesDto.breaks());
        schedule.setReservedHours(schedulesDto.reservedHours());

        return schedule;
    }

    private TimeslotSchedulesConfig getTimeSlotSchedulesConfig() {
        try {
            return TimeslotSchedulesConfig.loadFromFile(schedulesFilePath);
        } catch (IOException e) {
            throw new ServiceException.InternalServerError(SCHEDULE_MAPPING_ERROR_LOG_MESSAGE);
        }
    }
}
