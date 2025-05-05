package ru.tpu.hostel.schedules.external.rest.notification;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.tpu.hostel.schedules.external.rest.notification.dto.NotificationRequestDto;

@Component
@FeignClient(name = "notifications-notificationservice", url = "http://notificationservice:8080")
public interface NotificationClient {

    @PostMapping("/notifications")
    ResponseEntity<?> createNotification(@Valid @RequestBody NotificationRequestDto notificationRequestDto);
}
