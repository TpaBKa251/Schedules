package ru.tpu.hostel.schedules.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.tpu.hostel.schedules.dto.request.ResponsibleSetDto;
import ru.tpu.hostel.schedules.dto.response.ResponsibleResponseDto;
import ru.tpu.hostel.schedules.dto.response.UserShortResponseDto;
import ru.tpu.hostel.schedules.enums.EventType;
import ru.tpu.hostel.schedules.service.ResponsibleService;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/responsibles")
@RequiredArgsConstructor
public class ResponsibleController {

    private final ResponsibleService responsibleService;

    @PostMapping("/{type}")
    public ResponsibleResponseDto setResponsible(
            @PathVariable EventType type,
            @Valid @RequestBody ResponsibleSetDto responsibleSetDto
    ) {
        return responsibleService.setResponsible(type, responsibleSetDto);
    }

    @PostMapping("/self/{type}/{userId}/{userRoles}")
    public ResponsibleResponseDto setYourselfResponsible(
            @PathVariable EventType type,
            @PathVariable UUID userId,
            @PathVariable String[] userRoles,
            @Valid @RequestBody ResponsibleSetDto responsibleSetDto
    ) {
        return responsibleService.setYourselfResponsible(userId, userRoles, type, responsibleSetDto);
    }

    @GetMapping("/get/{date}/{type}")
    public UserShortResponseDto getResponsible(@PathVariable LocalDate date, @PathVariable EventType type) {
        return responsibleService.getResponsible(date, type);
    }

}
