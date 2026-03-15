
package com.example.backend.controller;

import com.example.backend.dto.BroneeringFilterRequest;
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

    @GetMapping
    public ResponseEntity<List<RestoraniLaud>> getAllLauad() {
        return ResponseEntity.ok(lauaService.getAllLauad());
    }

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
