package ru.tpu.hostel.schedules.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tpu.hostel.schedules.dto.response.TimeSlotResponse;
import ru.tpu.hostel.schedules.entity.TimeSlot;
import ru.tpu.hostel.schedules.mapper.TimeSlotMapper;
import ru.tpu.hostel.schedules.repository.TimeSlotRepository;
import ru.tpu.hostel.schedules.service.TimeSlotService;

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
}
