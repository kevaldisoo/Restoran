package com.example.backend;

import com.example.backend.model.*;
import com.example.backend.repository.BroneeringRepository;
import com.example.backend.repository.RestoraniLaudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RestoraniLaudRepository restoraniLaudRepository;
    private final BroneeringRepository broneeringRepository;

    @Override
    public void run(String... args) {
        if (restoraniLaudRepository.count() > 0) return;

        List<RestoraniLaud> lauad = createTables();
        restoraniLaudRepository.saveAll(lauad);
        broneeringRepository.saveAll(generateRandomBookings(lauad));
    }

    // ── Restorani plaan ────────────────────────────────────────────────────
    // Restorani mõõtmed: 900 × 680
    // Tsoonide ristkülikud:
    //   SISESAAL       x=10  y=40  w=545 h=400
    //   TERRASS      x=570 y=40  w=320 h=400
    //   PRIVAATRUUM x=10  y=455 w=880 h=215

    private List<RestoraniLaud> createTables() {
        List<RestoraniLaud> t = new ArrayList<>();

        // ── Sisesaal — 3 rida × 3-5 lauda (T01–T12) ──────────────────────
        // Rida 1 - 3 lauda, 10 kohta
        t.add(laud("T01", 10,  TsooniTyyp.SISESAAL, true, false,  30,  70, 100, 65));
        t.add(laud("T02", 10,  TsooniTyyp.SISESAAL, true, false, 210, 70, 100, 65));
        t.add(laud("T03", 10,  TsooniTyyp.SISESAAL, true, false, 390, 70, 100, 65));
        // Rida 2 - 4 lauda, 6 kohta
        t.add(laud("T04", 6,  TsooniTyyp.SISESAAL, false, true,  30,  200, 80, 55));
        t.add(laud("T05", 6,  TsooniTyyp.SISESAAL, false, true, 155, 200, 80, 55));
        t.add(laud("T06", 6,  TsooniTyyp.SISESAAL, false,  false, 280, 200, 80, 55));
        t.add(laud("T07", 6,  TsooniTyyp.SISESAAL, true,  false, 405, 200, 80, 55));
        // Rida 3 - 5 lauda, 2 kohta
        t.add(laud("T08", 2, TsooniTyyp.SISESAAL, false,  true, 30, 330, 60, 45));
        t.add(laud("T09", 2,  TsooniTyyp.SISESAAL, false, true,  125,  330, 60, 45));
        t.add(laud("T10", 2,  TsooniTyyp.SISESAAL, false, false, 220, 330, 60, 45));
        t.add(laud("T11", 2,  TsooniTyyp.SISESAAL, false, false, 315, 330, 60, 45));
        t.add(laud("T12", 2,  TsooniTyyp.SISESAAL, true,  false, 410, 330, 60, 45));

        // ── Terrass — 2 rida × 3 4-kohalist lauda (T13–T18) ────────────────────
        t.add(laud("T13", 4, TsooniTyyp.TERRASS, true, false,  615, 130, 70, 50));
        t.add(laud("T14", 4, TsooniTyyp.TERRASS, true, false, 700, 130, 70, 50));
        t.add(laud("T15", 4, TsooniTyyp.TERRASS, true, false, 785, 130, 70, 50));
        t.add(laud("T16", 4, TsooniTyyp.TERRASS, true, false,  615, 280, 70, 50));
        t.add(laud("T17", 4, TsooniTyyp.TERRASS, true, false, 700, 280, 70, 50));
        t.add(laud("T18", 4, TsooniTyyp.TERRASS, true, false, 785, 280, 70, 50));

        // ── Privaatruumid — 3 8-kohalist lauda (T19–T21) ─────────────────────────
        t.add(laud("T19", 8,  TsooniTyyp.PRIVAATRUUM, false, true,  45,  490, 90, 60));
        t.add(laud("T20", 8,  TsooniTyyp.PRIVAATRUUM, false,  false, 330, 490, 90, 60));
        t.add(laud("T21", 8, TsooniTyyp.PRIVAATRUUM, false,  false, 635, 490, 90, 60));

        return t;
    }

    private RestoraniLaud laud(String num, int cap, TsooniTyyp tsoon,
                                boolean aken, boolean lastenurk,
                                int x, int y, int w, int h) {
        return RestoraniLaud.builder()
                .lauaNumber(num).mahutavus(cap).tsoon(tsoon)
                .aknaAll(aken).lastenurk(lastenurk)
                .posX(x).posY(y).width(w).height(h)
                .build();
    }

    // ── Suvalised broneeringud ──────────────────────────────────────────────────────

    private static final String[] NAMES = {
        "Tamm", "Mägi", "Ivanov", "Puu", "Kask", "Saar",
        "Miller", "Rohi", "Sirge", "Anderson", "Mitt", "Maja",
        "Valge", "Roos", "Lips", "Kala", "Lind", "Karis"
    };

    // Suvalised broneeringute ajad, mis on vahemikus 2-3 tundi
    private static final LocalTime[][] TIME_SLOTS;

    static {
        TIME_SLOTS = new LocalTime[][]{
                {LocalTime.of(11, 30), LocalTime.of(13, 30)},
                {LocalTime.of(11, 30), LocalTime.of(14, 0)},
                {LocalTime.of(12, 0), LocalTime.of(14, 30)},
                {LocalTime.of(13, 0), LocalTime.of(15, 0)},
                {LocalTime.of(14, 30), LocalTime.of(16, 30)},
                {LocalTime.of(16, 0), LocalTime.of(18, 0)},
                {LocalTime.of(17, 0), LocalTime.of(19, 30)},
                {LocalTime.of(17, 30), LocalTime.of(20, 30)},
                {LocalTime.of(18, 0), LocalTime.of(21, 0)},
                {LocalTime.of(19, 0), LocalTime.of(21, 30)},
                {LocalTime.of(20, 0), LocalTime.of(22, 0)},
                {LocalTime.of(20, 30), LocalTime.of(23, 0)},
        };
    }

    // Genereerib juhuslikud broneeringud kõigile laudadele ajavahemikus täna kuni 6 päeva pärast.
    // Fikseeritud seemnega (42) tagab, et iga käivituse tulemus on sama. Küsitud abi Claude Code käest
    private List<Broneering> generateRandomBookings(List<RestoraniLaud> lauad) {
        Random rng = new Random(42); // fikseeritud seeme → taasesitatav saali plaan
        List<Broneering> broneeringud = new ArrayList<>();
        LocalDate tana = LocalDate.now();

        // Itereeri üle 7 päeva: täna (0) kuni 6 päeva pärast
        for (int dayOffset = 0; dayOffset <= 6; dayOffset++) {
            LocalDate date = tana.plusDays(dayOffset);

            // Iga laua jaoks proovi iga ajavahemikku
            for (RestoraniLaud laud : lauad) {
                for (LocalTime[] slot : TIME_SLOTS) {
                    // Jäta ~70% aegadest broneerimata
                    if (rng.nextDouble() > 0.30) continue;

                    LocalDateTime algus = LocalDateTime.of(date, slot[0]);
                    LocalDateTime lopp   = LocalDateTime.of(date, slot[1]);

                    // Jäta vahele, kui sellel laual on juba kattuv broneering
                    boolean konflikt = broneeringud.stream().anyMatch(b ->
                            b.getLaud() == laud
                            && b.getAlgusAeg().isBefore(lopp)
                            && b.getLoppAeg().isAfter(algus));
                    if (konflikt) continue;

                    // Külaliste arv vahemikus 1 kuni mahutavus
                    int kylalisteArv = Math.max(1, rng.nextInt(laud.getMahutavus()) + 1);
                    broneeringud.add(Broneering.builder()
                            .laud(laud)
                            .kylaline(NAMES[rng.nextInt(NAMES.length)] + " family")
                            .kylalisteArv(kylalisteArv)
                            .algusAeg(algus)
                            .loppAeg(lopp)
                            .staatus(BroneeringuStaatus.CONFIRMED)
                            .kommentaar("")
                            .build());
                }
            }
        }
        return broneeringud;
    }
}
