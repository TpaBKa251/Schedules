package ru.tpu.hostel.schedules.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
    HALL("Зал"),
    INTERNET("Интернет"),
    GYM("Тренажерный зал"),
    KITCHEN("Кухня");

    private final String eventTypeName;
}
