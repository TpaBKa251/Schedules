package ru.tpu.hostel.schedules.rabbit.amqp.timeslot.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ResponseStatus {

    SUCCESS("Успех"),
    FAILURE("Ошибка");

    private final String status;
}
