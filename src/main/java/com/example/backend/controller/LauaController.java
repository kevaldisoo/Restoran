
package com.example.backend.controller;

import com.example.backend.dto.BroneeringFilterRequest;
import com.example.backend.dto.LauaPositsioonDTO;
import com.example.backend.dto.KombineeritudLauadDTO;
import com.example.backend.dto.LauaSoovitusedDTO;
import com.example.backend.model.RestoraniLaud;
import com.example.backend.model.TsooniTyyp;
import com.example.backend.service.LauaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/tables")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class LauaController {

    private final LauaService lauaService;

    // Kõik lauad, mida näidatakse
    @GetMapping
    public ResponseEntity<List<RestoraniLaud>> getAllLauad() {
        return ResponseEntity.ok(lauaService.getAllLauad());
    }

    // Üksikute laudade soovitused
    @GetMapping("/recommendations")
    public ResponseEntity<List<LauaSoovitusedDTO>> getRecommendations(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate kuupaev,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime algusAeg,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime loppAeg,
            @RequestParam(defaultValue = "1") int kylalised,
            @RequestParam(required = false) TsooniTyyp tsoon,
            @RequestParam(required = false) Boolean aknaAll,
            @RequestParam(required = false) Boolean lastenurk,
            @RequestParam(required = false) Boolean privaatsus) {
        return ResponseEntity.ok(lauaService.getSoovitusi(
                buildFilter(kuupaev, algusAeg, loppAeg, kylalised, tsoon, aknaAll, lastenurk, privaatsus)));
    }

    // Kombineeritud laudade soovitused
    @GetMapping("/combined-recommendations")
    public ResponseEntity<List<KombineeritudLauadDTO>> getCombinedRecommendations(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate kuupaev,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime algusAeg,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime loppAeg,
            @RequestParam(defaultValue = "1") int kylalised,
            @RequestParam(required = false) TsooniTyyp tsoon,
            @RequestParam(required = false) Boolean aknaAll,
            @RequestParam(required = false) Boolean lastenurk,
            @RequestParam(required = false) Boolean privaatsus) {
        return ResponseEntity.ok(lauaService.getKombineeritudSoovitused(
                buildFilter(kuupaev, algusAeg, loppAeg, kylalised, tsoon, aknaAll, lastenurk, privaatsus)));
    }

    // Iga laua asukoht, mis uueneb liigutamisel
    @PutMapping("/{id}/position")
    public ResponseEntity<Void> updatePositsioon(
            @PathVariable Long id,
            @RequestBody LauaPositsioonDTO dto) {
        lauaService.updatePositsioon(id, dto);
        return ResponseEntity.ok().build();
    }

    private BroneeringFilterRequest buildFilter(LocalDate kuupaev, LocalTime algusAeg, LocalTime loppAeg,
                                                int kylalised, TsooniTyyp tsoon,
                                                Boolean aknaAll, Boolean lastenurk, Boolean privaatsus) {
        BroneeringFilterRequest filter = new BroneeringFilterRequest();
        filter.setKuupaev(kuupaev);
        filter.setAlgusAeg(algusAeg);
        filter.setLoppAeg(loppAeg);
        filter.setKylalisteArv(kylalised);
        filter.setTsoon(tsoon);
        filter.setAknaAll(aknaAll);
        filter.setLastenurk(lastenurk);
        filter.setPrivaatsus(privaatsus);
        return filter;
    }
}
