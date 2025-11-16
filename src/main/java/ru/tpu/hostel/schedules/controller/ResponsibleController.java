package ru.tpu.hostel.schedules.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.tpu.hostel.schedules.dto.request.ResponsibleEditRequestDto;
import ru.tpu.hostel.schedules.dto.request.ResponsibleSetRequestDto;
import ru.tpu.hostel.schedules.dto.response.ActiveEventResponseDto;
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
@Tag(name = "Ответственные за ресурс", description = "Эндпоинты для работы с ответственными на день за различные ресурсы")
@ApiResponse(responseCode = "500", description = "Неизвестная ошибка сервера", content = @Content)
@ApiResponse(
        responseCode = "400",
        description = "Неверный запрос от клиента, нарушение ограничений запроса (тело, параметры)",
        content = @Content
)
@ApiResponse(responseCode = "401", description = "Запрос не авторизован", content = @Content)
public class ResponsibleController {

    private final ResponsibleService responsibleService;

    @Operation(
            summary = "Создать ответственного",
            description = "Назначает указанного юзера ответственным за ресурс на указанный день. Если юзер не указан, то берется юзер, отправивший запрос",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Успешное создание (назначение) ответственного"),
                    @ApiResponse(responseCode = "403", description = "Нет прав управлять ответственными", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Нельзя назначить ответственного на указанный день, так как нет слотов для записи. Или юзер уже назначен. Или нарушение ограничений БД", content = @Content),
                    @ApiResponse(responseCode = "501", description = "Неизвестный тип ответственного", content = @Content)
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponsibleResponseDto setResponsible(
            @Valid @RequestBody ResponsibleSetRequestDto responsibleSetRequestDto
    ) {
        return responsibleService.setResponsible(responsibleSetRequestDto);
    }

    @Operation(
            summary = "Изменить ответственного",
            description = "Меняет юзера, который будет ответственным за ресурс на день. Если юзер не указан, то берется юзер, отправивший запрос",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Успешное создание (назначение) ответственного"),
                    @ApiResponse(responseCode = "403", description = "Нет прав управлять ответственными", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Ответственный не найден", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Кто-то уже изменил ответственного во время выполнения запроса. Или юзер уже назначен. Или нарушение ограничений БД", content = @Content)
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PatchMapping("{responsibleId}")
    public ResponsibleResponseDto editResponsible(
            @Parameter(description = "ID ответственного (записи в БД, не юзера)") @PathVariable UUID responsibleId,
            @RequestBody ResponsibleEditRequestDto responsibleEditRequestDto
    ) {
        return responsibleService.editResponsible(responsibleId, responsibleEditRequestDto);
    }

    @Operation(
            summary = "Получить одного ответственного (для ресурсов, где может быть только один ответственный за день)",
            description = "Меняет юзера, который будет ответственным за ресурс на день. Если юзер не указан, то берется юзер, отправивший запрос",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Успешное создание (назначение) ответственного"),
                    @ApiResponse(responseCode = "403", description = "Нет прав управлять ответственными", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Ответственный не найден", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Кто-то уже изменил ответственного во время выполнения запроса. Или юзер уже назначен. Или нарушение ограничений БД", content = @Content)
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
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

    @GetMapping("/active-event")
    public List<ActiveEventResponseDto> getActiveResponsible() {
        return responsibleService.getActiveResponsible();
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResponsible(@RequestParam(name = "responsibleId") UUID responsibleId) {
        responsibleService.deleteResponsible(responsibleId);
    }

}
