package ru.tpu.hostel.schedules.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.tpu.hostel.schedules.TestData;
import ru.tpu.hostel.schedules.service.KitchenSchedulesService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KitchenScheduleController.class)
@AutoConfigureMockMvc(addFilters = false)
class KitchenScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KitchenSchedulesService kitchenSchedulesService;

    @Test
    void getScheduleWhenFloorIsEmpty() throws Exception {
        when(kitchenSchedulesService.getSchedule()).thenReturn(List.of());

        mockMvc.perform(get("/schedules/kitchen/get/on/floor"))
                .andExpect(status().isOk());

        verify(kitchenSchedulesService).getSchedule();
    }

    @Test
    void getScheduleWhenFloorIsPresent() throws Exception {
        when(kitchenSchedulesService.getSchedule(TestData.FLOOR_2)).thenReturn(List.of());

        mockMvc.perform(get("/schedules/kitchen/get/on/floor").param("floor", TestData.FLOOR_2))
                .andExpect(status().isOk());

        verify(kitchenSchedulesService).getSchedule(TestData.FLOOR_2);
    }

    @Test
    void getActiveDutiesWhenIdentifierIsUuid() throws Exception {
        when(kitchenSchedulesService.getActiveDuties(TestData.USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/schedules/kitchen/get/on/room/{identifier}", TestData.USER_ID))
                .andExpect(status().isOk());

        verify(kitchenSchedulesService).getActiveDuties(TestData.USER_ID);
    }

    @Test
    void getActiveDutiesWhenIdentifierIsRoom() throws Exception {
        when(kitchenSchedulesService.getActiveDuties(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/schedules/kitchen/get/on/room/{identifier}", TestData.ROOM_NUMBER_201))
                .andExpect(status().isOk());
    }

    @Test
    void getDutyByIdWithSuccess() throws Exception {
        when(kitchenSchedulesService.getDutyById(TestData.KITCHEN_ID)).thenReturn(null);

        mockMvc.perform(get("/schedules/kitchen").param("kitchenScheduleId", TestData.KITCHEN_ID.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void swapDutiesWithSuccess() throws Exception {
        mockMvc.perform(patch("/schedules/kitchen/swap")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                TestData.swapRequestDto(TestData.KITCHEN_ID, TestData.OTHER_KITCHEN_ID))))
                .andExpect(status().isNoContent());

        verify(kitchenSchedulesService).swapDuties(any());
    }

    @Test
    void markDutyWithSuccess() throws Exception {
        mockMvc.perform(patch("/schedules/kitchen/mark/{kitchenScheduleId}", TestData.KITCHEN_ID))
                .andExpect(status().isNoContent());

        verify(kitchenSchedulesService).markDuty(TestData.KITCHEN_ID);
    }

    @Test
    void deleteDutyWithSuccess() throws Exception {
        mockMvc.perform(delete("/schedules/kitchen/{kitchenScheduleId}", TestData.KITCHEN_ID))
                .andExpect(status().isNoContent());

        verify(kitchenSchedulesService).deleteDutyById(TestData.KITCHEN_ID);
    }
}
