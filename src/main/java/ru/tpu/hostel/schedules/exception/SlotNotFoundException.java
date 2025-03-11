package ru.tpu.hostel.schedules.exception;

public class SlotNotFoundException extends RuntimeException {

    public SlotNotFoundException(String message) {
        super(message);
    }

    public SlotNotFoundException() {
        super("Слот не найден");
    }

}
