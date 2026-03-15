package com.example.backend.controller;

import com.example.backend.dto.BroneeringCreateRequest;
import com.example.backend.dto.BroneeringDTO;
import com.example.backend.model.BroneeringuStaatus;
import com.example.backend.service.BroneeringService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class BookingControllerTest {

    @Autowired
    private BroneeringService broneeringService;

    private BroneeringCreateRequest validRequest(long lauaId) {
        BroneeringCreateRequest req = new BroneeringCreateRequest();
        req.setLauaId(lauaId);
        req.setKylastaja("Jüri Mägi");
        req.setKylalisteArv(2);
        req.setAlgusAeg(LocalDateTime.of(2026, 9, 1, 19, 0));
        req.setLoppAeg(LocalDateTime.of(2026, 9, 1, 21, 0));
        req.setKommentaar("Sünnipäev");
        return req;
    }

    @Test
    void getAllBookings_returnsNonNullList() {
        List<BroneeringDTO> result = broneeringService.getAllBookings();
        assertThat(result).isNotNull();
    }

    @Test
    void createBooking_validRequest_returnsConfirmedBooking() {
        BroneeringDTO result = broneeringService.createBooking(validRequest(1L));
        assertThat(result.getId()).isNotNull();
        assertThat(result.getKylaline()).isEqualTo("Jüri Mägi");
        assertThat(result.getStaatus()).isEqualTo(BroneeringuStaatus.CONFIRMED);
    }

    @Test
    void createBooking_conflictingTime_throwsConflict() {
        broneeringService.createBooking(validRequest(2L));

        assertThatThrownBy(() -> broneeringService.createBooking(validRequest(2L)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(409));
    }

    @Test
    void createBooking_partySizeExceedsCapacity_throwsBadRequest() {
        BroneeringCreateRequest req = new BroneeringCreateRequest();
        req.setLauaId(1L);
        req.setKylastaja("Suur grupp");
        req.setKylalisteArv(10);
        req.setAlgusAeg(LocalDateTime.of(2026, 3, 20, 12, 0));
        req.setLoppAeg(LocalDateTime.of(2026, 3, 20, 14, 0));
        req.setKommentaar("");

        assertThatThrownBy(() -> broneeringService.createBooking(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(409));
    }

    @Test
    void createBooking_tableNotFound_throwsNotFound() {
        BroneeringCreateRequest req = new BroneeringCreateRequest();
        req.setLauaId(9999L);
        req.setKylastaja("m");
        req.setKylalisteArv(2);
        req.setAlgusAeg(LocalDateTime.of(2026, 9, 3, 19, 0));
        req.setLoppAeg(LocalDateTime.of(2026, 9, 3, 21, 0));
        req.setKommentaar("");

        assertThatThrownBy(() -> broneeringService.createBooking(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(404));
    }
}
