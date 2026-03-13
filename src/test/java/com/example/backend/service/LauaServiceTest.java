package com.example.backend.service;

import com.example.backend.dto.BroneeringFilterRequest;
import com.example.backend.dto.LauaSoovitusedDTO;
import com.example.backend.model.Booking;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LauaServiceTest {

    @Mock
    private RestoraniLaudRepository tableRepository;

    @Mock
    private BroneeringRepository broneeringRepository;

    @InjectMocks
    private LauaService tableService;

    private RestoraniLaud inside2p;
    private RestoraniLaud inside4p;
    private RestoraniLaud terrace4p;
    private RestoraniLaud private6p;

    @BeforeEach
    void setUp() {
        inside2p = RestoraniLaud.builder()
                .id(1L).tableNumber("T01").capacity(2)
                .zone(TsooniTyyp.INSIDE).windowView(false).accessible(true)
                .posX(30).posY(70).width(60).height(45).shape("rect").build();

        inside4p = RestoraniLaud.builder()
                .id(2L).tableNumber("T02").capacity(4)
                .zone(TsooniTyyp.INSIDE).windowView(true).accessible(false)
                .posX(155).posY(70).width(75).height(50).shape("rect").build();

        terrace4p = RestoraniLaud.builder()
                .id(3L).tableNumber("T13").capacity(4)
                .zone(TsooniTyyp.TERRACE).windowView(true).accessible(true)
                .posX(615).posY(130).width(35).height(35).shape("circle").build();

        private6p = RestoraniLaud.builder()
                .id(4L).tableNumber("T19").capacity(6)
                .zone(TsooniTyyp.PRIVATE_ROOM).windowView(false).accessible(true)
                .posX(45).posY(490).width(130).height(80).shape("rect").build();
    }

    private BroneeringFilterRequest filter(int partySize, TsooniTyyp zone) {
        BroneeringFilterRequest f = new BroneeringFilterRequest();
        f.setDate(LocalDate.now());
        f.setStartTime(LocalTime.of(19, 0));
        f.setEndTime(LocalTime.of(21, 0));
        f.setPartySize(partySize);
        f.setZone(zone);
        return f;
    }

    // ── Capacity filtering ────────────────────────────────────────────────────

    @Test
    void recommendations_excludesTablesWithInsufficientCapacity() {
        when(tableRepository.findAll()).thenReturn(List.of(inside2p, inside4p));
        when(broneeringRepository.findActiveBookingsBetween(any(), any())).thenReturn(List.of());

        List<LauaSoovitusedDTO> result = tableService.getSoovitusi(filter(3, null));

        assertThat(result).hasSize(2);
        assertThat(result).anySatisfy(d -> {
            assertThat(d.getLauaNumber()).isEqualTo("T02");
            assertThat(d.isMeetsFilter()).isTrue();
        });
        assertThat(result).anySatisfy(d -> {
            assertThat(d.getLauaNumber()).isEqualTo("T01");
            assertThat(d.isMeetsFilter()).isFalse(); // capacity 2 < 3
        });
    }

    @Test
    void recommendations_exactCapacityMatchScoresHigher() {
        when(tableRepository.findAll()).thenReturn(List.of(inside2p, inside4p));
        when(broneeringRepository.findActiveBookingsBetween(any(), any())).thenReturn(List.of());

        // partySize=2: T01 is exact match, T02 has excess seats
        List<LauaSoovitusedDTO> result = tableService.getSoovitusi(filter(2, null));

        LauaSoovitusedDTO best = result.stream()
                .filter(LauaSoovitusedDTO::isMeetsFilter)
                .findFirst().orElseThrow();
        assertThat(best.getLauaNumber()).isEqualTo("T01");
        assertThat(best.getSkoor()).isGreaterThan(
                result.stream().filter(d -> d.getLauaNumber().equals("T02"))
                        .findFirst().orElseThrow().getSkoor());
    }

    // ── Zone filtering ────────────────────────────────────────────────────────

    @Test
    void recommendations_filtersOutWrongZone() {
        when(tableRepository.findAll()).thenReturn(List.of(inside4p, terrace4p));
        when(broneeringRepository.findActiveBookingsBetween(any(), any())).thenReturn(List.of());

        List<LauaSoovitusedDTO> result = tableService.getSoovitusi(filter(2, TsooniTyyp.TERRASS));

        assertThat(result.stream().filter(d -> d.getTsoon() == TsooniTyyp.SISESAAL)
                .allMatch(d -> !d.isMeetsFilter())).isTrue();
        assertThat(result.stream().filter(d -> d.getTsoon() == TsooniTyyp.TERRASS)
                .allMatch(LauaSoovitusedDTO::isMeetsFilter)).isTrue();
    }

    // ── Availability ──────────────────────────────────────────────────────────

    @Test
    void recommendations_marksOccupiedTablesUnavailable() {
        Broneering existingBooking = Broneering.builder()
                .id(99L).laud(inside4p)
                .startTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(18, 0)))
                .endTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(20, 0)))
                .status(BroneeringuStaatus.CONFIRMED).build();

        when(tableRepository.findAll()).thenReturn(List.of(inside4p));
        when(broneeringRepository.findActiveBookingsBetween(any(), any()))
                .thenReturn(List.of(existingBooking));

        List<LauaSoovitusedDTO> result = tableService.getRecommendations(filter(2, null));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isAvailable()).isFalse();
        assertThat(result.get(0).getScore()).isNegative();
    }

    @Test
    void recommendations_freeTableIsAvailable() {
        when(tableRepository.findAll()).thenReturn(List.of(inside4p));
        when(broneeringRepository.findActiveBookingsBetween(any(), any())).thenReturn(List.of());

        List<LauaSoovitusedDTO> result = tableService.getRecommendations(filter(2, null));

        assertThat(result.get(0).isAvailable()).isTrue();
        assertThat(result.get(0).getScore()).isPositive();
    }

    // ── Preference scoring ────────────────────────────────────────────────────

    @Test
    void calculateScore_windowPreferenceBonusApplied() {
        BroneeringFilterRequest f = filter(2, null);
        f.setPreferWindowView(true);

        int withWindow    = tableService.calculateScore(inside4p, f); // windowView=true
        int withoutWindow = tableService.calculateScore(inside2p, f); // windowView=false

        assertThat(withWindow).isGreaterThan(withoutWindow);
    }

    @Test
    void calculateScore_accessibilityPreferenceBonusApplied() {
        BroneeringFilterRequest f = filter(2, null);
        f.setPreferAccessible(true);

        int withAccess    = tableService.calculateScore(inside2p, f);  // accessible=true
        int withoutAccess = tableService.calculateScore(inside4p, f);  // accessible=false

        // inside2p has +25 accessible bonus but also lower capacity excess penalty for party=2
        assertThat(withAccess).isGreaterThan(withoutAccess);
    }

    @Test
    void calculateScore_privacyPreferenceBonusApplied() {
        BroneeringFilterRequest f = filter(4, null);
        f.setPreferPrivacy(true);

        int privateScore = tableService.calculateScore(private6p, f);  // PRIVATE_ROOM
        int insideScore  = tableService.calculateScore(inside4p, f);   // INSIDE

        assertThat(privateScore).isGreaterThan(insideScore);
    }

    // ── Ordering ──────────────────────────────────────────────────────────────

    @Test
    void recommendations_sortedByScoreDescending() {
        when(tableRepository.findAll()).thenReturn(List.of(inside2p, inside4p, terrace4p, private6p));
        when(broneeringRepository.findActiveBookingsBetween(any(), any())).thenReturn(List.of());

        BroneeringFilterRequest f = filter(2, null);
        f.setPreferWindowView(true);
        f.setPreferAccessible(true);

        List<LauaSoovitusedDTO> result = tableService.getRecommendations(f);

        for (int i = 0; i < result.size() - 1; i++) {
            assertThat(result.get(i).getScore())
                    .isGreaterThanOrEqualTo(result.get(i + 1).getScore());
        }
    }
}
