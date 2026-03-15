package com.example.backend.service;

import com.example.backend.dto.BroneeringCreateRequest;
import com.example.backend.dto.BroneeringDTO;
import com.example.backend.model.Broneering;
import com.example.backend.model.BroneeringuStaatus;
import com.example.backend.model.RestoraniLaud;
import com.example.backend.model.TsooniTyyp;
import com.example.backend.repository.BroneeringRepository;
import com.example.backend.repository.RestoraniLaudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BroneeringServiceTest {

    @Mock private BroneeringRepository broneeringRepository;
    @Mock private RestoraniLaudRepository laudRepository;

    @InjectMocks
    private BroneeringService broneeringService;

    private RestoraniLaud laud;
    private BroneeringCreateRequest validRequest;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        laud = RestoraniLaud.builder()
                .id(1L).lauaNumber("T01").mahutavus(10)
                .tsoon(TsooniTyyp.SISESAAL).aknaAll(true).lastenurk(false)
                .posX(30).posY(70).width(100).height(65).build();

        start = LocalDateTime.of(LocalDate.now(), LocalTime.of(19, 0));
        end   = LocalDateTime.of(LocalDate.now(), LocalTime.of(21, 0));

        validRequest = new BroneeringCreateRequest();
        validRequest.setLauaId(1L);
        validRequest.setKylastaja("Test kylastaja");
        validRequest.setKylalisteArv(8);
        validRequest.setAlgusAeg(start);
        validRequest.setLoppAeg(end);
        validRequest.setKommentaar("test");
    }

    @Test
    void createBooking_successfullyCreatesBooking() {
        when(laudRepository.findById(1L)).thenReturn(Optional.of(laud));
        when(broneeringRepository.findConflictingBookings(any(), any(), any())).thenReturn(List.of());
        Broneering saved = Broneering.builder()
                .id(10L).laud(laud)
                .kylaline("Test kylastaja").kylalisteArv(8)
                .algusAeg(start).loppAeg(end).staatus(BroneeringuStaatus.CONFIRMED).kommentaar("test")
                .build();
        when(broneeringRepository.save(any(Broneering.class))).thenReturn(saved);

        BroneeringDTO result = broneeringService.createBooking(validRequest);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getLauaNumber()).isEqualTo("T01");
        assertThat(result.getKylaline()).isEqualTo("Test kylastaja");
        assertThat(result.getStaatus()).isEqualTo(BroneeringuStaatus.CONFIRMED);
        verify(broneeringRepository).save(any(Broneering.class));
    }

    @Test
    void createBooking_throwsNotFoundWhenTableMissing() {
        when(laudRepository.findById(99L)).thenReturn(Optional.empty());
        validRequest.setLauaId(99L);

        assertThatThrownBy(() -> broneeringService.createBooking(validRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Table not found");
    }

    @Test
    void createBooking_throwsBadRequestWhenPartySizeExceedsCapacity() {
        when(laudRepository.findById(1L)).thenReturn(Optional.of(laud));
        validRequest.setKylalisteArv(30);

        assertThatThrownBy(() -> broneeringService.createBooking(validRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("mahutavus");
    }

    @Test
    void createBooking_throwsConflictWhenTimeSlotTaken() {
        Broneering conflicting = Broneering.builder()
                .id(5L).laud(laud).staatus(BroneeringuStaatus.CONFIRMED)
                .algusAeg(start.minusHours(1)).loppAeg(end.minusHours(1)).build();

        when(laudRepository.findById(1L)).thenReturn(Optional.of(laud));
        when(broneeringRepository.findConflictingBookings(any(), any(), any()))
                .thenReturn(List.of(conflicting));

        assertThatThrownBy(() -> broneeringService.createBooking(validRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("juba broneeritud");
    }

    @Test
    void cancelBooking_setsStatusToCancelled() {
        Broneering booking = Broneering.builder()
                .id(10L).laud(laud).kylaline("Test").kylalisteArv(8)
                .algusAeg(start).loppAeg(end).staatus(BroneeringuStaatus.CONFIRMED).kommentaar("").build();

        when(broneeringRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(broneeringRepository.save(any(Broneering.class))).thenAnswer(inv -> inv.getArgument(0));

        BroneeringDTO result = broneeringService.cancelBooking(10L);

        assertThat(result.getStaatus()).isEqualTo(BroneeringuStaatus.CANCELLED);
    }

    @Test
    void cancelBooking_throwsNotFoundForMissingId() {
        when(broneeringRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> broneeringService.cancelBooking(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Booking not found");
    }

    @Test
    void getAllBookings_returnsAllAsDTOs() {
        Broneering b1 = Broneering.builder().id(1L).laud(laud).kylaline("A").kylalisteArv(2)
                .algusAeg(start).loppAeg(end).staatus(BroneeringuStaatus.CONFIRMED).kommentaar("").build();
        Broneering b2 = Broneering.builder().id(2L).laud(laud).kylaline("B").kylalisteArv(3)
                .algusAeg(start.plusDays(1)).loppAeg(end.plusDays(1))
                .staatus(BroneeringuStaatus.CONFIRMED).kommentaar("").build();

        when(broneeringRepository.findAll()).thenReturn(List.of(b1, b2));

        List<BroneeringDTO> result = broneeringService.getAllBookings();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(BroneeringDTO::getKylaline).containsExactlyInAnyOrder("A", "B");
    }
}
