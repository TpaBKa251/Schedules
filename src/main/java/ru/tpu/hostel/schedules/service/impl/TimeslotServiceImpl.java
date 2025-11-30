package ru.tpu.hostel.schedules.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tpu.hostel.internal.exception.ServiceException;
import ru.tpu.hostel.internal.utils.ExecutionContext;
import ru.tpu.hostel.internal.utils.TimeUtil;
import ru.tpu.hostel.schedules.dto.response.TimeslotResponse;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.entity.Timeslot;
import ru.tpu.hostel.schedules.external.rest.booking.BookingClient;
import ru.tpu.hostel.schedules.mapper.TimeslotMapper;
import ru.tpu.hostel.schedules.repository.TimeslotRepository;
import ru.tpu.hostel.schedules.service.TimeslotService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeslotServiceImpl implements TimeslotService {

    private static final String SLOT_UNAVAILABLE_EXCEPTION_MESSAGE = "Слот полностью заполнен";

    private static final String SLOT_EMPTY_EXCEPTION_MESSAGE = "Слот уже полностью свободен";

    private static final String ADD_SLOT_NOT_FOUND_EXCEPTION_MESSAGE = " или не найден";

    private static final String BAD_REQUEST_FOR_AVAILABLE_SLOTS_EXCEPTION_MESSAGE
            = "Вы можете просматривать и бронировать слоты только на неделю вперед";

    private final TimeslotRepository repository;

    private final BookingClient bookingClient;

    @Transactional
    @Override
    public Timeslot getTimeslotForBook(UUID slotId) {
        Timeslot timeSlot = repository.findAvailableSlotForUpdate(slotId, TimeUtil.now()).orElseThrow(() ->
                new ServiceException.Conflict(SLOT_UNAVAILABLE_EXCEPTION_MESSAGE + ADD_SLOT_NOT_FOUND_EXCEPTION_MESSAGE)
        );

        timeSlot.setBookingCount(timeSlot.getBookingCount() + 1);
        try {
            repository.flush();
            return timeSlot;
        } catch (DataIntegrityViolationException e) {
            throw new ServiceException.Conflict(SLOT_UNAVAILABLE_EXCEPTION_MESSAGE);
        }
    }

    @Transactional
    @Override
    public void cancelTimeSlot(UUID slotId) {
        Timeslot timeSlot = repository.findSlotForUpdate(slotId).orElseThrow(()
                -> new ServiceException.Conflict(SLOT_EMPTY_EXCEPTION_MESSAGE + ADD_SLOT_NOT_FOUND_EXCEPTION_MESSAGE)
        );

        timeSlot.setBookingCount(timeSlot.getBookingCount() - 1);
        try {
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ServiceException.Conflict(SLOT_EMPTY_EXCEPTION_MESSAGE);
        }
    }

    @Override
    public List<TimeslotResponse> getAvailableTimeBooking(LocalDate date, EventType bookingType) {
        if (TimeUtil.now().toLocalDate().plusDays(7).isBefore(date)
                || date.isBefore(TimeUtil.now().toLocalDate())) {
            throw new ServiceException.BadRequest(BAD_REQUEST_FOR_AVAILABLE_SLOTS_EXCEPTION_MESSAGE);
        }

        final List<UUID> bookedTimeslotsIds = new ArrayList<>();
        try {
            bookedTimeslotsIds.addAll(
                    bookingClient.getAllByStatusShort(ExecutionContext.get().getUserID(), date, bookingType)
            );
        } catch (Exception e) {
            log.warn("Не удалось получить забронированные слоты", e);
        }

        LocalDateTime startTime = date == TimeUtil.now().toLocalDate()
                ? TimeUtil.now()
                : date.atStartOfDay();

        return repository.findAllAvailableTimeslotsOnDay(
                        bookingType,
                        date.atStartOfDay().plusDays(1),
                        startTime
                )
                .stream()
                .map(timeslot ->
                        TimeslotMapper.mapTimeSlotToTimeSlotResponse(
                                timeslot,
                                bookedTimeslotsIds.contains(timeslot.getId())
                        )
                )
                .toList();
    }

}
