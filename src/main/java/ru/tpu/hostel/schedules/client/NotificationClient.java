package ru.tpu.hostel.schedules.client;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.Serializable;
import java.util.UUID;

@Component
@FeignClient(name = "notifications-notificationservice", url = "http://notificationservice:8080")
public interface NotificationClient {

    @PostMapping("/notifications")
    ResponseEntity<?> createNotification(@Valid @RequestBody NotificationRequestDto notificationRequestDto);

    record NotificationRequestDto(
            @NotNull(message = "ID пользователя не может быть пустым")
            UUID userId,

            @NotNull(message = "Тип уведомления не может быть пустым")
            NotificationType type,

            @NotNull(message = "Заголовок не может быть пустым")
            @NotBlank(message = "Заголовок не может быть пустым")
            @Length(message = "Заголовок должен быть в пределах от 1 до 100 символов", min = 1, max = 100)
            String title,

            @NotNull(message = "Заголовок не может быть пустым")
            @NotBlank(message = "Заголовок не может быть пустым")
            @Length(message = "Заголовок должен быть в пределах от 1 до 1000 символов", min = 1, max = 1000)
            String message
    ) implements Serializable {
    }

    enum NotificationType {
        KITCHEN_SCHEDULE(),
        ;
    }
}
