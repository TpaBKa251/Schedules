package ru.tpu.hostel.schedules.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.tpu.hostel.schedules.TestData;
import ru.tpu.hostel.schedules.dto.response.TimeslotResponse;
import ru.tpu.hostel.schedules.entity.EventType;
import ru.tpu.hostel.schedules.service.TimeslotService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TimeslotController.class)
@AutoConfigureMockMvc(addFilters = false)
class TimeslotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TimeslotService timeslotService;

    @Test
    void getAvailableTimeBookingWithSuccess() throws Exception {
        TimeslotResponse response = new TimeslotResponse(
                TestData.TIMESLOT_ID, "09:00", "09:45",
                TestData.BOOKING_COUNT_0, TestData.LIMIT_8, false);
        when(timeslotService.getAvailableTimeBooking(eq(TestData.DATE_TODAY), eq(EventType.GYM)))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/schedules/available/timeslot/{date}/{bookingType}",
                        TestData.DATE_TODAY.toString(), EventType.GYM.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void deleteTimeSlotWithSuccess() throws Exception {
        mockMvc.perform(delete("/schedules/available/timeslot")
                        .param("timeSlotIds", TestData.TIMESLOT_ID.toString()))
                .andExpect(status().isOk());

        verify(timeslotService).deleteTimeSlots(any());
    }
}
