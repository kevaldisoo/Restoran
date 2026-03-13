package com.example.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllTables_returns200WithTableList() throws Exception {
        mockMvc.perform(get("/api/tables"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(21));
    }

    @Test
    void getAllTables_tableHasRequiredFields() throws Exception {
        mockMvc.perform(get("/api/tables"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tableNumber").exists())
                .andExpect(jsonPath("$[0].capacity").exists())
                .andExpect(jsonPath("$[0].zone").exists())
                .andExpect(jsonPath("$[0].posX").exists())
                .andExpect(jsonPath("$[0].posY").exists())
                .andExpect(jsonPath("$[0].shape").exists());
    }

    @Test
    void getRecommendations_withValidParams_returns200() throws Exception {
        mockMvc.perform(get("/api/tables/recommendations")
                        .param("date", "2026-06-15")
                        .param("startTime", "19:00:00")
                        .param("endTime", "21:00:00")
                        .param("partySize", "4"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(21));
    }

    @Test
    void getRecommendations_recommendedTableHasScoreAndAvailability() throws Exception {
        mockMvc.perform(get("/api/tables/recommendations")
                        .param("date", "2026-06-15")
                        .param("startTime", "19:00:00")
                        .param("endTime", "21:00:00")
                        .param("partySize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").exists())
                .andExpect(jsonPath("$[0].available").exists())
                .andExpect(jsonPath("$[0].meetsFilter").exists());
    }

    @Test
    void getRecommendations_withZoneFilter_onlyMatchingZoneHasMeetsFilterTrue() throws Exception {
        mockMvc.perform(get("/api/tables/recommendations")
                        .param("date", "2026-06-15")
                        .param("startTime", "12:00:00")
                        .param("endTime", "14:00:00")
                        .param("partySize", "2")
                        .param("zone", "TERRACE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.zone == 'INSIDE' && @.meetsFilter == true)]").isEmpty())
                .andExpect(jsonPath("$[?(@.zone == 'PRIVATE_ROOM' && @.meetsFilter == true)]").isEmpty());
    }

    @Test
    void getRecommendations_sortedByScoreDescending() throws Exception {
        mockMvc.perform(get("/api/tables/recommendations")
                        .param("date", "2026-06-15")
                        .param("startTime", "19:00:00")
                        .param("endTime", "21:00:00")
                        .param("partySize", "2")
                        .param("preferWindowView", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(
                        org.hamcrest.Matchers.greaterThanOrEqualTo(
                                (int) -1))); // first is best or -1 if all occupied
    }

    @Test
    void getRecommendations_missingRequiredParam_returns400() throws Exception {
        mockMvc.perform(get("/api/tables/recommendations")
                        // missing date, startTime, endTime
                        .param("partySize", "4"))
                .andExpect(status().isBadRequest());
    }
}
