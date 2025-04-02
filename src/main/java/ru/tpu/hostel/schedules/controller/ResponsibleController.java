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

@RestController
@RequestMapping("/responsibles")
@RequiredArgsConstructor
public class ResponsibleController {

    private final ResponsibleService responsibleService;

    @PostMapping
    public ResponsibleResponseDto setResponsible(@Valid @RequestBody ResponsibleSetDto responsibleSetDto) {
        return responsibleService.setResponsible(responsibleSetDto);
    }

    @GetMapping("/get/{date}/{type}")
    public UserShortResponseDto getResponsible(@PathVariable LocalDate date, @PathVariable EventType type) {
        return responsibleService.getResponsible(date, type);
    }

    @GetMapping
    public String test(){
        return "helo";
    }
}
