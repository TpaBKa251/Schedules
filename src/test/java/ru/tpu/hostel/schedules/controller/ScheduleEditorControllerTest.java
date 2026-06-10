package ru.tpu.hostel.schedules.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.tpu.hostel.schedules.TestData;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.service.editor.configs.SchedulesEditorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScheduleEditorController.class)
@AutoConfigureMockMvc(addFilters = false)
class ScheduleEditorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SchedulesEditorService schedulesEditorService;

    @Test
    void editScheduleWithSuccess() throws Exception {
        mockMvc.perform(patch("/schedules/edit/{eventType}", EventType.GYM.name())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(TestData.schedulesDto())))
                .andExpect(status().isNoContent());

        verify(schedulesEditorService).editSchedule(any(), any());
    }

    @Test
    void getScheduleWithSuccess() throws Exception {
        when(schedulesEditorService.getSchedule(EventType.GYM)).thenReturn(TestData.schedulesDto());

        mockMvc.perform(get("/schedules/{eventType}", EventType.GYM.name()))
                .andExpect(status().isOk());
    }
}
