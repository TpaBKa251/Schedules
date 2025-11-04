package ru.tpu.hostel.schedules.dto.response;

import java.util.UUID;

public record UserNameWithIdResponse(
        UUID id,
        String firstName,
        String lastName,
        String middleName,
        String tgLink,
        String vkLink
) {
}
