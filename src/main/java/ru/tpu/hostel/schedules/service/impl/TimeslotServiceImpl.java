package ru.tpu.hostel.schedules.service.impl;

import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.tpu.hostel.internal.exception.ServiceException;
import ru.tpu.hostel.internal.utils.TimeUtil;
import ru.tpu.hostel.schedules.dto.response.TimeslotResponse;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.entity.Timeslot;
import ru.tpu.hostel.schedules.mapper.TimeslotMapper;
import ru.tpu.hostel.schedules.repository.TimeslotRepository;
import ru.tpu.hostel.schedules.service.TimeslotService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimeslotServiceImpl implements TimeslotService {

    private final TimeslotRepository repository;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Override
    public Timeslot getTimeslotForBook(UUID slotId) {
        Timeslot timeSlot = repository.findAvailableSlotForUpdate(slotId, TimeUtil.now())
                .orElseThrow(() -> new ServiceException.Conflict("Слот заполнен или не найден"));

        timeSlot.setBookingCount(timeSlot.getBookingCount() + 1);
        try {
            return repository.save(timeSlot);
        } catch (ConstraintViolationException e) {
            throw new ServiceException.Conflict("Слот полностью заполнен");
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Override
    public void cancelTimeSlot(UUID slotId) {
        Timeslot timeSlot = repository.findSlotForUpdate(slotId)
                .orElseThrow(() -> new ServiceException.Conflict("Слот свободен или не найден"));

        timeSlot.setBookingCount(timeSlot.getBookingCount() - 1);
        try {
            repository.save(timeSlot);
        } catch (ConstraintViolationException e) {
            throw new ServiceException.Conflict("Слот уже полностью свободен");
        }
    }

    @Override
    public List<TimeslotResponse> getAvailableTimeBooking(LocalDate date, EventType bookingType) {
        if (TimeUtil.now().toLocalDate().plusDays(7).isBefore(date)
                || date.isBefore(TimeUtil.now().toLocalDate())) {
            throw new ServiceException.BadRequest("Вы можете просматривать и бронировать слоты только на неделю вперед");
        }

        return repository.findAllAvailableTimeslotsOnDay(bookingType, date, TimeUtil.now()).stream()
                .map(TimeslotMapper::mapTimeSlotToTimeSlotResponse)
                .toList();
    }

}
