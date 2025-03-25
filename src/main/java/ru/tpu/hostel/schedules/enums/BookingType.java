package ru.tpu.hostel.schedules.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BookingType {
    HALL("Зал"),
    INTERNET("Интернет"),
    GYM("Тренажерный зал"),
    KITCHEN("Кухня");

    private final String bookingTypeName;
}
