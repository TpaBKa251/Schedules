package ru.tpu.hostel.schedules.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.tpu.hostel.schedules.dto.request.ResponsibleEditRequestDto;
import ru.tpu.hostel.schedules.dto.request.ResponsibleSetRequestDto;
import ru.tpu.hostel.schedules.dto.response.ResponsibleResponseDto;
import ru.tpu.hostel.schedules.dto.response.UserNameWithIdResponse;
import ru.tpu.hostel.schedules.dto.response.UserShortResponseDto;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.service.ResponsibleService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/responsibles")
@RequiredArgsConstructor
public class ResponsibleController {

    private final ResponsibleService responsibleService;

    @PostMapping
    public ResponsibleResponseDto setResponsible(@Valid @RequestBody ResponsibleSetRequestDto responsibleSetRequestDto) {
        return responsibleService.setResponsible(responsibleSetRequestDto);
    }

    @PatchMapping("{responsibleId}")
    public ResponsibleResponseDto editResponsible(
            @PathVariable UUID responsibleId,
            @RequestBody ResponsibleEditRequestDto responsibleEditRequestDto
    ) {
        return responsibleService.editResponsible(responsibleId, responsibleEditRequestDto);
    }

    @GetMapping("/one")
    public UserShortResponseDto getResponsible(
            @RequestParam(name = "date") LocalDate date,
            @RequestParam(name = "type") EventType type
    ) {
        return responsibleService.getResponsibleByTypeAndDate(date, type);
    }

    @GetMapping("/many")
    public List<UserNameWithIdResponse> getAllResponsible(
            @RequestParam(name = "date") LocalDate date,
            @RequestParam(name = "type") EventType type
    ) {
        return responsibleService.getAllResponsibleByTypeAndDate(date, type);
    }

}
