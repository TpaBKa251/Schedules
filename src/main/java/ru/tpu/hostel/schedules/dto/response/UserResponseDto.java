package ru.tpu.hostel.schedules.dto.response;

public record UserResponseDto(
        String firstName,
        String lastName,
        String middleName,
        String roomNumber
) {
}
