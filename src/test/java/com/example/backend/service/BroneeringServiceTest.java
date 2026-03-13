package com.example.backend.service;

import com.example.backend.dto.BookingCreateRequest;
import com.example.backend.dto.BookingDTO;
import com.example.backend.model.Booking;
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
    @Mock private RestoraniLaudRepository tableRepository;

    @InjectMocks
    private BroneeringService broneeringService;

    private RestoraniLaud table;
    private BookingCreateRequest validRequest;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        table = RestoraniLaud.builder()
                .id(1L).tableNumber("T01").capacity(4)
                .zone(TsooniTyyp.INSIDE).windowView(false).accessible(true)
                .posX(30).posY(70).width(75).height(50).shape("rect").build();

        start = LocalDateTime.of(LocalDate.now(), LocalTime.of(19, 0));
        end   = LocalDateTime.of(LocalDate.now(), LocalTime.of(21, 0));

        validRequest = new BookingCreateRequest();
        validRequest.setTableId(1L);
        validRequest.setCustomerName("Test Customer");
        validRequest.setPartySize(3);
        validRequest.setStartTime(start);
        validRequest.setEndTime(end);
        validRequest.setNotes("Anniversary dinner");
    }

    @Test
    void createBooking_successfullyCreatesBooking() {
        when(tableRepository.findById(1L)).thenReturn(Optional.of(table));
        when(broneeringRepository.findConflictingBookings(any(), any(), any())).thenReturn(List.of());
        Booking saved = Booking.builder()
                .id(10L).table(table)
                .customerName("Test Customer").partySize(3)
                .startTime(start).endTime(end).status(BroneeringuStaatus.CONFIRMED).notes("Anniversary dinner")
                .build();
        when(broneeringRepository.save(any(Booking.class))).thenReturn(saved);

        BookingDTO result = broneeringService.createBooking(validRequest);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getTableNumber()).isEqualTo("T01");
        assertThat(result.getCustomerName()).isEqualTo("Test Customer");
        assertThat(result.getStatus()).isEqualTo(BroneeringuStaatus.CONFIRMED);
        verify(broneeringRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_throwsNotFoundWhenTableMissing() {
        when(tableRepository.findById(99L)).thenReturn(Optional.empty());
        validRequest.setTableId(99L);

        assertThatThrownBy(() -> broneeringService.createBooking(validRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Table not found");
    }

    @Test
    void createBooking_throwsBadRequestWhenPartySizeExceedsCapacity() {
        when(tableRepository.findById(1L)).thenReturn(Optional.of(table));
        validRequest.setPartySize(10); // table capacity is 4

        assertThatThrownBy(() -> broneeringService.createBooking(validRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("capacity");
    }

    @Test
    void createBooking_throwsConflictWhenTimeSlotTaken() {
        Booking conflicting = Booking.builder()
                .id(5L).table(table).status(BroneeringuStaatus.CONFIRMED)
                .startTime(start.minusHours(1)).endTime(end.minusHours(1)).build();

        when(tableRepository.findById(1L)).thenReturn(Optional.of(table));
        when(broneeringRepository.findConflictingBookings(any(), any(), any()))
                .thenReturn(List.of(conflicting));

        assertThatThrownBy(() -> broneeringService.createBooking(validRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already booked");
    }

    @Test
    void cancelBooking_setsStatusToCancelled() {
        Booking booking = Booking.builder()
                .id(10L).table(table).customerName("Test").partySize(2)
                .startTime(start).endTime(end).status(BroneeringuStaatus.CONFIRMED).notes("").build();

        when(broneeringRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(broneeringRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingDTO result = broneeringService.cancelBooking(10L);

        assertThat(result.getStatus()).isEqualTo(BroneeringuStaatus.CANCELLED);
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
        Booking b1 = Booking.builder().id(1L).table(table).customerName("A").partySize(2)
                .startTime(start).endTime(end).status(BroneeringuStaatus.CONFIRMED).notes("").build();
        Booking b2 = Booking.builder().id(2L).table(table).customerName("B").partySize(3)
                .startTime(start.plusDays(1)).endTime(end.plusDays(1))
                .status(BroneeringuStaatus.CONFIRMED).notes("").build();

        when(broneeringRepository.findAll()).thenReturn(List.of(b1, b2));

        List<BookingDTO> result = broneeringService.getAllBookings();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(BookingDTO::getCustomerName).containsExactlyInAnyOrder("A", "B");
    }
}
