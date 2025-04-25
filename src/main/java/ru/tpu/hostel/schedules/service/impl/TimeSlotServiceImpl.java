package ru.tpu.hostel.schedules.service.impl;

import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Service;
import ru.tpu.hostel.schedules.dto.response.TimeSlotResponse;
import ru.tpu.hostel.schedules.entity.TimeSlot;
import ru.tpu.hostel.schedules.exception.ServiceException;
import ru.tpu.hostel.schedules.mapper.TimeSlotMapper;
import ru.tpu.hostel.schedules.repository.TimeSlotRepository;
import ru.tpu.hostel.schedules.service.TimeSlotService;
import ru.tpu.hostel.schedules.utils.TimeNow;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimeSlotServiceImpl implements TimeSlotService {

    private final TimeSlotRepository repository;

    @Override
    public TimeSlotResponse getTimeSlotById(UUID uuid) {
        TimeSlot timeSlot = repository.findById(uuid).orElse(null);
        return timeSlot == null
                ? null
                : TimeSlotMapper.mapTimeSlotToTimeSlotResponse(repository.save(timeSlot));
    }

    @Override
    public TimeSlot bookTimeslot(UUID slotId) {
        TimeSlot timeSlot = repository.findAvailableSlotForUpdate(slotId, TimeNow.now())
                .orElseThrow(() -> new ServiceException.BadRequest("Слот заполнен или не найден"));

        timeSlot.setBookingCount(timeSlot.getBookingCount() + 1);
        try {
            return repository.save(timeSlot);
        } catch (ConstraintViolationException e) {
            throw new ServiceException.BadRequest("Слот заполнен или не найден");
        }
    }

}
