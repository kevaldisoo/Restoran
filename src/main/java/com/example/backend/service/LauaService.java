package com.example.backend.service;

import com.example.backend.dto.BroneeringFilterRequest;
import com.example.backend.dto.LauaSoovitusedDTO;
import com.example.backend.model.RestoraniLaud;
import com.example.backend.model.TsooniTyyp;
import com.example.backend.repository.BroneeringRepository;
import com.example.backend.repository.RestoraniLaudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LauaService {

    private final RestoraniLaudRepository lauaRepository;
    private final BroneeringRepository broneeringRepository;

    public List<RestoraniLaud> getAllLauad() {
        return lauaRepository.findAll();
    }

    /**
     * Tagastab kõik lauad, et saaliplaanis oleksid värvitud iga laud,
     * mitte ainult vabad lauad.
     */
    public List<LauaSoovitusedDTO> getSoovitusi(BroneeringFilterRequest filter) {
        LocalDateTime algusDt = LocalDateTime.of(filter.getKuupaev(), filter.getAlgusAeg());
        LocalDateTime loppDt   = LocalDateTime.of(filter.getKuupaev(), filter.getLoppAeg());

        Set<Long> occupiedIds = broneeringRepository
                .findActiveBookingsBetween(algusDt, loppDt)
                .stream()
                .map(b -> b.getLaud().getId())
                .collect(Collectors.toSet());

        List<RestoraniLaud> all = lauaRepository.findAll();

        return all.stream()
                .map(t -> {
                    boolean available   = !occupiedIds.contains(t.getId());
                    boolean meetsFilter = meetsFilter(t, filter);
                    int score = (available && meetsFilter)
                            ? calculateScore(t, filter)
                            : (available ? 0 : -1);
                    return LauaSoovitusedDTO.from(t, available, meetsFilter, score);
                })
                .sorted(Comparator.comparingInt(LauaSoovitusedDTO::getSkoor).reversed())
                .collect(Collectors.toList());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private boolean meetsFilter(RestoraniLaud t, BroneeringFilterRequest f) {
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
