package com.example.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private Map<String, Object> validBookingBody(long tableId) {
        return Map.of(
                "tableId",      tableId,
                "customerName", "Jane Doe",
                "partySize",    2,
                "startTime",    "2026-09-01T19:00:00",
                "endTime",      "2026-09-01T21:00:00",
                "notes",        "Birthday"
        );
    }

    @Test
    void getAllBookings_returns200WithArray() throws Exception {
        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createBooking_validRequest_returns201() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookingBody(1L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customerName").value("Jane Doe"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void createBooking_conflictingTime_returns409() throws Exception {
        // First booking
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookingBody(2L))))
                .andExpect(status().isCreated());

        // Second booking for the same table and overlapping time
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookingBody(2L))))
                .andExpect(status().isConflict());
    }

    @Test
    void createBooking_partySizeExceedsCapacity_returns400() throws Exception {
        // T01 has capacity 2; request partySize=10
        Map<String, Object> body = Map.of(
                "tableId",      1L,
                "customerName", "Big Group",
                "partySize",    10,
                "startTime",    "2026-09-02T12:00:00",
                "endTime",      "2026-09-02T14:00:00",
                "notes",        ""
        );

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_tableNotFound_returns404() throws Exception {
        Map<String, Object> body = Map.of(
                "tableId",      9999L,
                "customerName", "Nobody",
                "partySize",    2,
                "startTime",    "2026-09-03T19:00:00",
                "endTime",      "2026-09-03T21:00:00",
                "notes",        ""
        );

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelBooking_existingId_returns200WithCancelledStatus() throws Exception {
        // Create first, then cancel
        String response = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "tableId",      3L,
                                "customerName", "Cancel Test",
                                "partySize",    2,
                                "startTime",    "2026-09-04T10:00:00",
                                "endTime",      "2026-09-04T12:00:00",
                                "notes",        ""
                        ))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long createdId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/bookings/" + createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelBooking_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/bookings/999999"))
                .andExpect(status().isNotFound());
    }
}
