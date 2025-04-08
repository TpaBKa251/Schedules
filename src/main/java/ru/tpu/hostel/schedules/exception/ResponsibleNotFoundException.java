package ru.tpu.hostel.schedules.exception;

public class ResponsibleNotFoundException extends NotFoundException {
    public ResponsibleNotFoundException() {
        super("Ответственный не найден");
    }

}
