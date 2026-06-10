package ru.tpu.hostel.schedules;

import ru.tpu.hostel.schedules.config.schedule.RoomsConfig;
import ru.tpu.hostel.schedules.config.schedule.TimeslotSchedulesConfig;
import ru.tpu.hostel.schedules.dto.SchedulesDto;
import ru.tpu.hostel.schedules.dto.request.ResponsibleEditRequestDto;
import ru.tpu.hostel.schedules.dto.request.ResponsibleSetRequestDto;
import ru.tpu.hostel.schedules.dto.request.SwapRequestDto;
import ru.tpu.hostel.schedules.dto.response.UserNameWithIdResponse;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.entity.KitchenSchedule;
import ru.tpu.hostel.schedules.entity.Responsible;
import ru.tpu.hostel.schedules.entity.Timeslot;
import ru.tpu.hostel.schedules.external.rest.user.dto.UserResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class TestData {

    public static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    public static final UUID OTHER_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    public static final UUID RESPONSIBLE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    public static final UUID OTHER_RESPONSIBLE_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    public static final UUID KITCHEN_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");

    public static final UUID OTHER_KITCHEN_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");

    public static final UUID TIMESLOT_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");

    public static final UUID OTHER_TIMESLOT_ID = UUID.fromString("88888888-8888-8888-8888-888888888888");

    public static final String FIRST_NAME_IVAN = "Иван";

    public static final String LAST_NAME_IVANOV = "Иванов";

    public static final String MIDDLE_NAME = "Иванович";

    public static final String TG_LINK = "kik_butovski";

    public static final String VK_LINK = "vk_butovski";

    public static final String ROOM_NUMBER_201 = "201";

    public static final String ROOM_NUMBER_202 = "202";

    public static final String ROOM_NUMBER_301 = "301";

    public static final String FLOOR_2 = "2";

    public static final String FLOOR_3 = "3";

    public static final String EMPTY = "";

    public static final int LIMIT_5 = 5;

    public static final int LIMIT_8 = 8;

    public static final int BOOKING_COUNT_0 = 0;

    public static final int BOOKING_COUNT_1 = 1;

    public static final int SCHEDULE_NUMBER_1 = 1;

    public static final int YEAR_2025 = 2025;

    public static final int MONTH_3 = 3;

    public static final int DAY_10 = 10;

    public static final int HOUR_9 = 9;

    public static final int HOUR_10 = 10;

    public static final int MINUTE_0 = 0;

    public static final int MINUTE_45 = 45;

    public static final int SLOT_DURATION_MINUTES = 45;

    public static final String SCHEDULES_FILE_PATH = "schedules.json";

    public static final String GYM_TYPE = "GYM";

    public static final LocalDate DATE_TODAY = LocalDate.of(YEAR_2025, MONTH_3, DAY_10);

    public static final LocalDateTime NOW = LocalDateTime.of(YEAR_2025, MONTH_3, DAY_10, HOUR_9, MINUTE_0);

    public static final LocalDateTime NOW_AFTERNOON = LocalDateTime.of(YEAR_2025, MONTH_3, DAY_10, 15, MINUTE_0);

    public static final LocalDateTime SLOT_START = LocalDateTime.of(YEAR_2025, MONTH_3, DAY_10, HOUR_9, MINUTE_0);

    public static final LocalDateTime SLOT_END = LocalDateTime.of(YEAR_2025, MONTH_3, DAY_10, HOUR_9, MINUTE_45);

    public static final LocalTime WORK_START = LocalTime.of(HOUR_9, MINUTE_0);

    public static final LocalTime WORK_END = LocalTime.of(11, MINUTE_0);

    private TestData() {
    }

    public static Timeslot newTimeslot(
            UUID id,
            LocalDateTime startTime,
            LocalDateTime endTime,
            EventType type,
            int limit,
            int bookingCount
    ) {
        Timeslot timeslot = new Timeslot();
        timeslot.setId(id);
        timeslot.setStartTime(startTime);
        timeslot.setEndTime(endTime);
        timeslot.setType(type);
        timeslot.setLimit(limit);
        timeslot.setBookingCount(bookingCount);
        timeslot.setVersion(0L);
        return timeslot;
    }

    public static Timeslot defaultTimeslot() {
        return newTimeslot(TIMESLOT_ID, SLOT_START, SLOT_END, EventType.GYM, LIMIT_8, BOOKING_COUNT_0);
    }

    public static KitchenSchedule newKitchenSchedule(
            UUID id,
            LocalDate date,
            String roomNumber,
            boolean checked
    ) {
        KitchenSchedule kitchenSchedule = new KitchenSchedule();
        kitchenSchedule.setId(id);
        kitchenSchedule.setDate(date);
        kitchenSchedule.setRoomNumber(roomNumber);
        kitchenSchedule.setChecked(checked);
        kitchenSchedule.setScheduleNumber(SCHEDULE_NUMBER_1);
        kitchenSchedule.setVersion(0L);
        return kitchenSchedule;
    }

    public static KitchenSchedule defaultKitchenSchedule() {
        return newKitchenSchedule(KITCHEN_ID, DATE_TODAY, ROOM_NUMBER_201, false);
    }

    public static Responsible newResponsible(UUID id, EventType type, LocalDate date, UUID user) {
        Responsible responsible = new Responsible();
        responsible.setId(id);
        responsible.setType(type);
        responsible.setDate(date);
        responsible.setUser(user);
        responsible.setVersion(0L);
        return responsible;
    }

    public static Responsible defaultResponsible() {
        return newResponsible(RESPONSIBLE_ID, EventType.GYM, DATE_TODAY, USER_ID);
    }

    public static UserResponseDto userResponseDto() {
        return new UserResponseDto(
                FIRST_NAME_IVAN,
                LAST_NAME_IVANOV,
                MIDDLE_NAME,
                ROOM_NUMBER_201,
                TG_LINK,
                VK_LINK
        );
    }

    public static UserNameWithIdResponse userNameWithIdResponse(UUID id) {
        return new UserNameWithIdResponse(
                id,
                FIRST_NAME_IVAN,
                LAST_NAME_IVANOV,
                MIDDLE_NAME,
                TG_LINK,
                VK_LINK
        );
    }

    public static ResponsibleSetRequestDto responsibleSetRequestDto(LocalDate date, EventType type, UUID user) {
        return new ResponsibleSetRequestDto(date, type, user);
    }

    public static ResponsibleEditRequestDto responsibleEditRequestDto(UUID user) {
        return new ResponsibleEditRequestDto(user);
    }

    public static SwapRequestDto swapRequestDto(UUID dutyId1, UUID dutyId2) {
        return new SwapRequestDto(dutyId1, dutyId2);
    }

    public static SchedulesDto schedulesDto() {
        TimeslotSchedulesConfig.TimeRange workingHours = new TimeslotSchedulesConfig.TimeRange();
        workingHours.setStart(WORK_START);
        workingHours.setEnd(WORK_END);
        TimeslotSchedulesConfig.BreakConfig breaks = new TimeslotSchedulesConfig.BreakConfig();
        breaks.setAfterSlots(2);
        breaks.setBreakDurationMinutes(15);
        return new SchedulesDto(
                LIMIT_8,
                USER_ID,
                List.of("Monday", "Wednesday", "Friday"),
                workingHours,
                SLOT_DURATION_MINUTES,
                breaks,
                new HashMap<>()
        );
    }

    public static TimeslotSchedulesConfig.TimeRange timeRange(LocalTime start, LocalTime end, boolean endNextDay) {
        TimeslotSchedulesConfig.TimeRange timeRange = new TimeslotSchedulesConfig.TimeRange();
        timeRange.setStart(start);
        timeRange.setEnd(end);
        timeRange.setEndNextDay(endNextDay);
        return timeRange;
    }

    public static TimeslotSchedulesConfig.Schedule gymSchedule() {
        TimeslotSchedulesConfig.Schedule schedule = new TimeslotSchedulesConfig.Schedule();
        schedule.setType(GYM_TYPE);
        schedule.setLimit(LIMIT_8);
        schedule.setWorkingDays(new ArrayList<>(List.of(
                "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
        )));
        schedule.setWorkingHours(timeRange(WORK_START, WORK_END, false));
        schedule.setSlotDurationMinutes(SLOT_DURATION_MINUTES);
        schedule.setBreaks(null);
        schedule.setReservedHours(new HashMap<>());
        return schedule;
    }

    public static TimeslotSchedulesConfig gymConfig() {
        TimeslotSchedulesConfig config = new TimeslotSchedulesConfig();
        Map<String, TimeslotSchedulesConfig.Schedule> schedules = new HashMap<>();
        schedules.put("gym", gymSchedule());
        config.setSchedules(schedules);
        return config;
    }

    public static RoomsConfig roomsConfig() {
        RoomsConfig roomsConfig = new RoomsConfig();
        roomsConfig.setSecondFloor(new ArrayList<>(List.of(ROOM_NUMBER_201, ROOM_NUMBER_202)));
        roomsConfig.setThirdFloor(new ArrayList<>(List.of(ROOM_NUMBER_301)));
        roomsConfig.setFourthFloor(new ArrayList<>(List.of("401")));
        roomsConfig.setFifthFloor(new ArrayList<>(List.of("501")));
        return roomsConfig;
    }
}
