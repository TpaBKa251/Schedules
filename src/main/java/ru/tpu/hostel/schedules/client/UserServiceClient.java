package ru.tpu.hostel.schedules.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.tpu.hostel.schedules.dto.response.UserResponseDto;

import java.util.List;
import java.util.UUID;

@Component
@FeignClient(name = "user-userservice", url = "http://userservice:8080")
public interface UserServiceClient {

    @GetMapping("/users/get/all/on/floor/{userId}")
    List<UserResponseDto> getAllOnFloor(@PathVariable UUID userId);

    @GetMapping("/users/get/room/{userId}")
    String getRoomNumber(@PathVariable UUID userId);

    @GetMapping("/users/get/all/in/rooms")
    List<UserResponseDto> getAllInRooms(@RequestParam String[] roomNumbers);

    @GetMapping("/users/get/all/in/rooms/with/id")
    List<UUID> getAllInRoomsWithId(@RequestParam String[] roomNumbers);

    @GetMapping("/roles/get/user/roles/all/{userId}")
    List<String> getAllRolesByUserId(@PathVariable UUID userId);
}
