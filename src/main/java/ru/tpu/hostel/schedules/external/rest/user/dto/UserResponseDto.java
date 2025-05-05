package ru.tpu.hostel.schedules.external.rest.user.dto;

public record UserResponseDto(
        String firstName,
        String lastName,
        String middleName,
        String roomNumber
) {
}
