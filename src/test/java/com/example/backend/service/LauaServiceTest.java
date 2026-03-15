package com.example.backend.service;

import com.example.backend.dto.BroneeringFilterRequest;
import com.example.backend.dto.LauaSoovitusedDTO;
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
    private RestoraniLaudRepository laudRepository;

    @Mock
    private BroneeringRepository broneeringRepository;

    @InjectMocks
    private LauaService lauaService;

    private RestoraniLaud sees2inimest;
    private RestoraniLaud sees6inimest;
    private RestoraniLaud terrass4inimest;
    private RestoraniLaud privaat8inimest;

    @BeforeEach
    void setUp() {
        sees2inimest = RestoraniLaud.builder()
                .id(1L).lauaNumber("T08").mahutavus(2)
                .tsoon(TsooniTyyp.SISESAAL).aknaAll(false).lastenurk(true)
                .posX(30).posY(330).width(60).height(45).build();

        sees6inimest = RestoraniLaud.builder()
                .id(2L).lauaNumber("T05").mahutavus(6)
                .tsoon(TsooniTyyp.SISESAAL).aknaAll(false).lastenurk(true)
                .posX(155).posY(200).width(80).height(55).build();

        terrass4inimest = RestoraniLaud.builder()
                .id(3L).lauaNumber("T13").mahutavus(4)
                .tsoon(TsooniTyyp.TERRASS).aknaAll(true).lastenurk(false)
                .posX(615).posY(130).width(70).height(50).build();

        privaat8inimest = RestoraniLaud.builder()
                .id(4L).lauaNumber("T19").mahutavus(8)
                .tsoon(TsooniTyyp.PRIVAATRUUM).aknaAll(false).lastenurk(true)
                .posX(45).posY(490).width(90).height(60).build();
    }

    private BroneeringFilterRequest filter(int kylalisteArv, TsooniTyyp tsoon) {
        BroneeringFilterRequest f = new BroneeringFilterRequest();
        f.setKuupaev(LocalDate.now());
        f.setAlgusAeg(LocalTime.of(19, 0));
        f.setLoppAeg(LocalTime.of(21, 0));
        f.setKylalisteArv(kylalisteArv);
        f.setTsoon(tsoon);
        return f;
    }

    // ── Mahutavuse filtreerimine  ────────────────────────────────────────────────────

    @Test
    void recommendations_excludesTablesWithInsufficientCapacity() {
        when(laudRepository.findAll()).thenReturn(List.of(sees2inimest, sees6inimest));
        when(broneeringRepository.findActiveBookingsBetween(any(), any())).thenReturn(List.of());

        List<LauaSoovitusedDTO> result = lauaService.getSoovitusi(filter(3, null));

        assertThat(result).hasSize(2);
        assertThat(result).anySatisfy(d -> {
            assertThat(d.getLauaNumber()).isEqualTo("T05");
            assertThat(d.isMeetsFilter()).isTrue();
        });
        assertThat(result).anySatisfy(d -> {
            assertThat(d.getLauaNumber()).isEqualTo("T08");
            assertThat(d.isMeetsFilter()).isFalse(); // mahutavus 2 < 3
        });
    }

    @Test
    void soovitused_skoorOnSuuremKuiTapneArv() {
        when(laudRepository.findAll()).thenReturn(List.of(sees2inimest, sees6inimest));
        when(broneeringRepository.findActiveBookingsBetween(any(), any())).thenReturn(List.of());

        // kui kylaliste arv on 2, siis T08 sobib täpselt, T05 on liiga palju
        List<LauaSoovitusedDTO> result = lauaService.getSoovitusi(filter(2, null));

        LauaSoovitusedDTO best = result.stream()
                .filter(LauaSoovitusedDTO::isMeetsFilter)
                .findFirst().orElseThrow();
        assertThat(best.getLauaNumber()).isEqualTo("T08");
        assertThat(best.getSkoor()).isGreaterThan(
                result.stream().filter(d -> d.getLauaNumber().equals("T05"))
                        .findFirst().orElseThrow().getSkoor());
    }

    // ── Tsooni filtreerimine ────────────────────────────────────────────────────────

    @Test
    void soovitused_valeTsooniValjaFiltreerimine() {
        when(laudRepository.findAll()).thenReturn(List.of(sees6inimest, terrass4inimest));
        when(broneeringRepository.findActiveBookingsBetween(any(), any())).thenReturn(List.of());

        List<LauaSoovitusedDTO> result = lauaService.getSoovitusi(filter(2, TsooniTyyp.TERRASS));

        assertThat(result.stream().filter(d -> d.getTsoon() == TsooniTyyp.SISESAAL)
                .allMatch(d -> !d.isMeetsFilter())).isTrue();
        assertThat(result.stream().filter(d -> d.getTsoon() == TsooniTyyp.TERRASS)
                .allMatch(LauaSoovitusedDTO::isMeetsFilter)).isTrue();
    }

    // ── Saadavus ──────────────────────────────────────────────────────────

    @Test
    void soovitused_broneeritudLauduEiSaaValida() {
        Broneering existingBooking = Broneering.builder()
                .id(99L).laud(sees6inimest)
                .algusAeg(LocalDateTime.of(LocalDate.now(), LocalTime.of(18, 0)))
                .loppAeg(LocalDateTime.of(LocalDate.now(), LocalTime.of(20, 0)))
                .staatus(BroneeringuStaatus.CONFIRMED).build();

        when(laudRepository.findAll()).thenReturn(List.of(sees6inimest));
        when(broneeringRepository.findActiveBookingsBetween(any(), any()))
                .thenReturn(List.of(existingBooking));

        List<LauaSoovitusedDTO> result = lauaService.getSoovitusi(filter(2, null));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isVaba()).isFalse();
        assertThat(result.get(0).getSkoor()).isNegative();
    }

    @Test
    void soovitused_vabaLaudOnSaadaval() {
        when(laudRepository.findAll()).thenReturn(List.of(sees6inimest));
        when(broneeringRepository.findActiveBookingsBetween(any(), any())).thenReturn(List.of());

        List<LauaSoovitusedDTO> result = lauaService.getSoovitusi(filter(2, null));

        assertThat(result.get(0).isVaba()).isTrue();
        assertThat(result.get(0).getSkoor()).isPositive();
    }

    // ── Eelistustega skoor ────────────────────────────────────────────────────

    @Test
    void skooriArvutus_eelistusegaAknaAll() {
        BroneeringFilterRequest f = filter(2, null);
        f.setAknaAll(true);

        int aknaga = lauaService.arvutaSkoor(terrass4inimest, f);
        int aknata = lauaService.arvutaSkoor(sees6inimest, f);

        assertThat(aknaga).isGreaterThan(aknata);
    }

    @Test
    void skooriArvutus_eelistusegaLastenurgaJuures() {
        BroneeringFilterRequest f = filter(2, null);
        f.setLastenurk(true);

        int lastenurgaga    = lauaService.arvutaSkoor(sees2inimest, f);
        int lastenurgata = lauaService.arvutaSkoor(terrass4inimest, f);

        assertThat(lastenurgaga).isGreaterThan(lastenurgata);
    }

    @Test
    void skooriArvutus_eelistusegaPrivaatsus() {
        BroneeringFilterRequest f = filter(4, null);
        f.setPrivaatsus(true);

        int privaatsusSkoor = lauaService.arvutaSkoor(privaat8inimest, f);
        int sisesaalSkoor  = lauaService.arvutaSkoor(sees6inimest, f);

        assertThat(privaatsusSkoor).isGreaterThan(sisesaalSkoor);
    }

    // ── Tellimine ──────────────────────────────────────────────────────────────

    @Test
    void soovitused_SkoorLangevasJarjestuses() {
        when(laudRepository.findAll()).thenReturn(List.of(sees2inimest, sees6inimest, terrass4inimest, privaat8inimest));
        when(broneeringRepository.findActiveBookingsBetween(any(), any())).thenReturn(List.of());

        BroneeringFilterRequest f = filter(2, null);
        f.setAknaAll(true);
        f.setLastenurk(true);

        List<LauaSoovitusedDTO> result = lauaService.getSoovitusi(f);

        for (int i = 0; i < result.size() - 1; i++) {
            assertThat(result.get(i).getSkoor())
                    .isGreaterThanOrEqualTo(result.get(i + 1).getSkoor());
        }
    }
}
