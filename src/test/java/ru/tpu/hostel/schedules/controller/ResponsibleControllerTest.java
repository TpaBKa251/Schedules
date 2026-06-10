package ru.tpu.hostel.schedules.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.tpu.hostel.schedules.TestData;
import ru.tpu.hostel.schedules.dto.response.UserShortResponseDto;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.service.ResponsibleService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResponsibleController.class)
@AutoConfigureMockMvc(addFilters = false)
class ResponsibleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ResponsibleService responsibleService;

    @Test
    void setResponsibleWithSuccess() throws Exception {
        when(responsibleService.setResponsible(any())).thenReturn(null);

        mockMvc.perform(post("/responsibles")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                TestData.responsibleSetRequestDto(TestData.DATE_TODAY, EventType.GYM, TestData.USER_ID))))
                .andExpect(status().isCreated());
    }

    @Test
    void editResponsibleWithSuccess() throws Exception {
        when(responsibleService.editResponsible(eq(TestData.RESPONSIBLE_ID), any())).thenReturn(null);

        mockMvc.perform(patch("/responsibles/{responsibleId}", TestData.RESPONSIBLE_ID)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                TestData.responsibleEditRequestDto(TestData.USER_ID))))
                .andExpect(status().isOk());
    }

    @Test
    void getResponsibleWithSuccess() throws Exception {
        UserShortResponseDto dto = new UserShortResponseDto(
                TestData.FIRST_NAME_IVAN, TestData.LAST_NAME_IVANOV, TestData.MIDDLE_NAME,
                TestData.TG_LINK, TestData.VK_LINK);
        when(responsibleService.getResponsibleByTypeAndDate(TestData.DATE_TODAY, EventType.GYM)).thenReturn(dto);

        mockMvc.perform(get("/responsibles/one")
                        .param("date", TestData.DATE_TODAY.toString())
                        .param("type", EventType.GYM.name()))
                .andExpect(status().isOk());
    }

    @Test
    void getAllResponsibleWithSuccess() throws Exception {
        when(responsibleService.getAllResponsibleByTypeAndDate(TestData.DATE_TODAY, EventType.GYM))
                .thenReturn(List.of(TestData.userNameWithIdResponse(TestData.USER_ID)));

        mockMvc.perform(get("/responsibles/many")
                        .param("date", TestData.DATE_TODAY.toString())
                        .param("type", EventType.GYM.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getActiveResponsibleWithSuccess() throws Exception {
        when(responsibleService.getActiveResponsible()).thenReturn(List.of());

        mockMvc.perform(get("/responsibles/active-event"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteResponsibleWithSuccess() throws Exception {
        mockMvc.perform(delete("/responsibles")
                        .param("userId", TestData.USER_ID.toString())
                        .param("type", EventType.GYM.name())
                        .param("date", TestData.DATE_TODAY.toString()))
                .andExpect(status().isNoContent());

        verify(responsibleService).deleteResponsible(TestData.USER_ID, EventType.GYM, TestData.DATE_TODAY);
    }
}
