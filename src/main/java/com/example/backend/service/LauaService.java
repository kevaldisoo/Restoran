package com.example.backend.service;

import com.example.backend.dto.BroneeringFilterRequest;
import com.example.backend.dto.LauaSoovitusedDTO;
import com.example.backend.model.RestoraniLaud;
import com.example.backend.model.TsooniTyyp;
import com.example.backend.repository.BroneeringRepository;
import com.example.backend.repository.RestoraniLaudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.example.backend.dto.KombineeritudLauadDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.example.backend.dto.LauaPositsioonDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class LauaService {

    private final RestoraniLaudRepository lauaRepository;
    private final BroneeringRepository broneeringRepository;
    private final Map<Long, LauaPositsioonDTO> positsioonOverride = new ConcurrentHashMap<>();

    public List<RestoraniLaud> getAllLauad() {
        return lauaRepository.findAll().stream()
                .map(this::applyOverride)
                .collect(Collectors.toList());
    }

    /**
     * Tagasta kõik soovitused
     * @param filter Külastaja poolt valitud filtrid
     * @return Tagasta lauad, mis ei ole broneeritud, ning järjesta need vastavalt skoorile
     */
    public List<LauaSoovitusedDTO> getSoovitusi(BroneeringFilterRequest filter) {
        LocalDateTime algusDt = LocalDateTime.of(filter.getKuupaev(), filter.getAlgusAeg());
        LocalDateTime loppDt   = LocalDateTime.of(filter.getKuupaev(), filter.getLoppAeg());

        Set<Long> occupiedIds = broneeringRepository
                .findActiveBookingsBetween(algusDt, loppDt)
                .stream()
                .map(b -> b.getLaud().getId())
                .collect(Collectors.toSet());

        List<RestoraniLaud> all = lauaRepository.findAll().stream()
                .map(this::applyOverride)
                .toList();

        return all.stream()
                .map(t -> {
                    boolean vaba = !occupiedIds.contains(t.getId());
                    boolean meetsFilter = sobibFiltriga(t, filter);
                    int score = (vaba && meetsFilter)
                            ? calculateScore(t, filter)
                            : (vaba ? 0 : -1);
                    return LauaSoovitusedDTO.from(t, vaba, meetsFilter, score);
                })
                .sorted(Comparator.comparingInt(LauaSoovitusedDTO::getSkoor).reversed())
                .collect(Collectors.toList());
    }

    public void updatePositsioon(Long id, LauaPositsioonDTO dto) {
        if (!lauaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lauda ei leitud: " + id);
        }
        positsioonOverride.put(id, dto);
    }

    /**
     * Muuda positsioone ka backendis, kui on muudetud admin lehel
     * @param t Laua informaatsioon
     * @return Uuenenud laua informatsioon
     */
    private RestoraniLaud applyOverride(RestoraniLaud t) {
        LauaPositsioonDTO ov = positsioonOverride.get(t.getId());
        if (ov == null) return t; // Kui pole muutunud, ära tee midagi
        int x = ov.getPosX(), y = ov.getPosY();
        return RestoraniLaud.builder()
                .id(t.getId())
                .lauaNumber(t.getLauaNumber())
                .mahutavus(t.getMahutavus())
                .tsoon(t.getTsoon())
                .aknaAll(computeAknaAll(x, y, t.getTsoon()))
                .lastenurk(computeLastenurk(x, y, t.getWidth(), t.getHeight()))
                .posX(x).posY(y)
                .width(t.getWidth()).height(t.getHeight())
                .build();
    }

    /**
     * Leia kas laud on akna all või mitte
     * @param posX Laua X positsioon
     * @param posY Laua Y positsioon
     * @param tsoon Tsoon, kus laud asub
     * @return Laud on akna all juhul, kui X on suurem kui 400, Y on väiiksem kui 150 või laud on terrassil. Privaatruum ei saa kunagi olla akna all.
     */
    private static boolean computeAknaAll(int posX, int posY, TsooniTyyp tsoon) {
        if (tsoon == TsooniTyyp.PRIVAATRUUM) return false;
        return posY < 150 || posX > 400 || tsoon == TsooniTyyp.TERRASS;
    }

    /**
     * Arvuta, kas laud on lastenurga lähedal
     * @param posX Laua X positsioon
     * @param posY Laua Y positsioon
     * @param width Laua laius
     * @param height Laua pikkus
     * @return Laud on lastenurga juures siis, kui on 200 ühiku raadiuses.
     */

    private static boolean computeLastenurk(int posX, int posY, int width, int height) {
        // Lastenurga punkt on (10, 350)
        double cx = posX + width / 2.0;
        double cy = posY + height / 2.0;
        return Math.sqrt((cx - 10) * (cx - 10) + (cy - 350) * (cy - 350)) <= 200;
    }

    /**
     * Tagastab paarid laudadest, mida saab kokku lükata, kui ükski üksik laud ei mahu suurele grupile.
     * Tingimused: mõlemad lauad vabad, samas tsoonis, sama rida (posY ühtib), x-serv kaugus alt; 100 SVG ühikut.
     */
    public List<KombineeritudLauadDTO> getKombineeritudSoovitused(BroneeringFilterRequest filter) {
        log.info("getKombineeritudSoovitused: kylalisteArv={}, kuupaev={}, algus={}, lopp={}",
                filter.getKylalisteArv(), filter.getKuupaev(), filter.getAlgusAeg(), filter.getLoppAeg());

        List<LauaSoovitusedDTO> soovitused = getSoovitusi(filter);
        log.info("  Kokku laudu: {}", soovitused.size());

        // Pakume kombineeringut ainult siis, kui ükski üksik laud ei mahu
        boolean hasSingleFit = soovitused.stream()
                .anyMatch(r -> r.isVaba() && r.getMahutavus() >= filter.getKylalisteArv());
        log.info("  hasSingleFit={}", hasSingleFit);
        if (hasSingleFit) {
            log.info("  Üksik laud mahub — kombineeritud soovitused vahele jäetud");
            return List.of();
        }

        List<LauaSoovitusedDTO> vabad = soovitused.stream()
                .filter(LauaSoovitusedDTO::isVaba)
                .collect(Collectors.toList());
        log.info("  Vabade laudade arv: {}", vabad.size());
        vabad.forEach(l -> log.info("    vaba laud: {} tsoon={} mahutavus={} posX={} posY={} w={} h={}",
                l.getLauaNumber(), l.getTsoon(), l.getMahutavus(), l.getPosX(), l.getPosY(), l.getWidth(), l.getHeight()));

        List<KombineeritudLauadDTO> kombineeringud = new ArrayList<>();

        for (int i = 0; i < vabad.size(); i++) {
            for (int j = i + 1; j < vabad.size(); j++) {
                LauaSoovitusedDTO l1 = vabad.get(i);
                LauaSoovitusedDTO l2 = vabad.get(j);

                if (l1.getTsoon() != l2.getTsoon()) {
                    log.debug("  {} + {}: erinev tsoon ({} vs {})", l1.getLauaNumber(), l2.getLauaNumber(), l1.getTsoon(), l2.getTsoon());
                    continue;
                }

                if (l1.getPosY() != l2.getPosY()) {
                    log.info("  {} + {}: erinev rida (posY {} vs {})", l1.getLauaNumber(), l2.getLauaNumber(), l1.getPosY(), l2.getPosY());
                    continue;
                }

                int hGap = xGap(l1, l2);
                if (hGap >= 100) {
                    log.info("  {} + {}: liiga kaugel x-teljel (hGap={})", l1.getLauaNumber(), l2.getLauaNumber(), hGap);
                    continue;
                }

                int combinedCap = l1.getMahutavus() + l2.getMahutavus();
                if (combinedCap < filter.getKylalisteArv()) {
                    log.info("  {} + {}: kombineeritud mahutavus {} < {}", l1.getLauaNumber(), l2.getLauaNumber(), combinedCap, filter.getKylalisteArv());
                    continue;
                }

                log.info("  {} + {}: SOBIB (hGap={}, kombineeritudMahutavus={})", l1.getLauaNumber(), l2.getLauaNumber(), hGap, combinedCap);
                kombineeringud.add(new KombineeritudLauadDTO(
                        l1, l2, combinedCap, calculateCombinedScore(l1, l2, filter)));
            }
        }

        log.info("  Kombineeringute arv: {}", kombineeringud.size());
        kombineeringud.sort(Comparator.comparingInt(KombineeritudLauadDTO::getSkoor).reversed());
        return kombineeringud;
    }

    /** Arvutab serv-serv kauguse x-teljel kahe ristküliku vahel. */
    private int xGap(LauaSoovitusedDTO a, LauaSoovitusedDTO b) {
        return Math.max(0, Math.max(a.getPosX(), b.getPosX())
                - Math.min(a.getPosX() + a.getWidth(), b.getPosX() + b.getWidth()));
    }

    /**
     * Arvutame kombineeritud skoori
     * @param l1 Esimese laua info
     * @param l2 Teise laua info
     * @param f Külastaja filtreeringu eelistused
     * @return Skoor, mis lauad kokku saavad, kui need kombineerida
     */
    private int calculateCombinedScore(LauaSoovitusedDTO l1, LauaSoovitusedDTO l2, BroneeringFilterRequest f) {
        int skoor = 100;
        int ylejaak = (l1.getMahutavus() + l2.getMahutavus()) - f.getKylalisteArv();
        skoor -= ylejaak * 10;
        if (Boolean.TRUE.equals(f.getAknaAll()) && (l1.isAknaAll() || l2.isAknaAll()))       skoor += 25;
        if (Boolean.TRUE.equals(f.getLastenurk()) && (l1.isLastenurk() || l2.isLastenurk())) skoor += 25;
        if (Boolean.TRUE.equals(f.getPrivaatsus()) && l1.getTsoon() == TsooniTyyp.PRIVAATRUUM) skoor += 25;
        return skoor;
    }

    // ── Abistajad ──────────────────────────────────────────────────────────────

    /**
     * Kontrollime, ega soovitused ei lähe külastaja filtreerimisega vastuollu
     */
    private boolean sobibFiltriga(RestoraniLaud t, BroneeringFilterRequest f) {
        if (t.getMahutavus() < f.getKylalisteArv()) return false;
        if (f.getTsoon() != null && t.getTsoon() != f.getTsoon()) return false;
        return true;
    }

    /**
     * Suurem skoor = rohkem soovitatud.
     * Algne skoor 100, maha läheb punkte kui on kohti üle ning eelistused annavad punkte juurde.
     */
    int calculateScore(RestoraniLaud t, BroneeringFilterRequest f) {
        int skoor = 100;

        // Eelistame, et toolid ei jääks tühjaks
        int ylejaak = t.getMahutavus() - f.getKylalisteArv();
        skoor -= ylejaak * 10;

        // Anname 25 punkti juurde juhul kui eelistus klapib
        if (Boolean.TRUE.equals(f.getAknaAll()) && t.isAknaAll()) skoor += 25;
        if (Boolean.TRUE.equals(f.getLastenurk()) && t.isLastenurk())  skoor += 25;
        if (Boolean.TRUE.equals(f.getPrivaatsus())
                && t.getTsoon() == TsooniTyyp.PRIVAATRUUM)                       skoor += 25;

        return skoor;
    }
}
