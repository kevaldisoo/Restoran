package com.example.backend.controller;

import com.example.backend.dto.BroneeringFilterRequest;
import com.example.backend.dto.LauaSoovitusedDTO;
import com.example.backend.model.RestoraniLaud;
import com.example.backend.model.TsooniTyyp;
import com.example.backend.service.LauaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TableControllerTest {

    @Autowired
    private LauaService lauaService;

    private BroneeringFilterRequest filter(int kylalised) {
        BroneeringFilterRequest f = new BroneeringFilterRequest();
        f.setKuupaev(LocalDate.of(2026, 6, 15));
        f.setAlgusAeg(LocalTime.of(19, 0));
        f.setLoppAeg(LocalTime.of(21, 0));
        f.setKylalisteArv(kylalised);
        return f;
    }

    @Test
    void getAllLauad_tagastab21Lauda() {
        List<RestoraniLaud> lauad = lauaService.getAllLauad();
        assertThat(lauad).hasSize(21);
    }

    @Test
    void getAllLauad_laualOnOlemasAndmed() {
        RestoraniLaud laud = lauaService.getAllLauad().getFirst();
        assertThat(laud.getLauaNumber()).isNotNull();
        assertThat(laud.getMahutavus()).isPositive();
        assertThat(laud.getTsoon()).isNotNull();
        assertThat(laud.getPosX()).isGreaterThanOrEqualTo(0);
        assertThat(laud.getPosY()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void saaSoovitusi_headeFiltritega_tagastab21Lauda() {
        List<LauaSoovitusedDTO> result = lauaService.getSoovitusi(filter(4));
        assertThat(result).hasSize(21);
    }

    @Test
    void saaSoovitusi_laudadelOnSkoorJaMahutavus() {
        List<LauaSoovitusedDTO> result = lauaService.getSoovitusi(filter(2));
        LauaSoovitusedDTO first = result.getFirst();
        assertThat(first.getSkoor()).isNotNull();
        assertThat(first.isMeetsFilter()).isNotNull();
    }

    @Test
    void saaSoovitusi_terrassiFiltriga_naitabAinultTerrassiLaudu() {
        BroneeringFilterRequest f = filter(2);
        f.setTsoon(TsooniTyyp.TERRASS);

        List<LauaSoovitusedDTO> result = lauaService.getSoovitusi(f);

        assertThat(result)
                .filteredOn(r -> r.getTsoon() == TsooniTyyp.SISESAAL)
                .allMatch(r -> !r.isMeetsFilter());
        assertThat(result)
                .filteredOn(r -> r.getTsoon() == TsooniTyyp.PRIVAATRUUM)
                .allMatch(r -> !r.isMeetsFilter());
    }

    @Test
    void saaSoovitusi_skoorideJarjestusOnLangev() {
        BroneeringFilterRequest f = filter(2);
        f.setAknaAll(true);

        List<LauaSoovitusedDTO> result = lauaService.getSoovitusi(f);

        for (int i = 0; i < result.size() - 1; i++) {
            assertThat(result.get(i).getSkoor()).isGreaterThanOrEqualTo(result.get(i + 1).getSkoor());
        }
    }
}
