package com.example.backend.service;

import com.example.backend.dto.BroneeringCreateRequest;
import com.example.backend.dto.BroneeringDTO;
import com.example.backend.model.Broneering;
import com.example.backend.model.BroneeringuStaatus;
import com.example.backend.model.RestoraniLaud;
import com.example.backend.repository.BroneeringRepository;
import com.example.backend.repository.RestoraniLaudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BroneeringService {

    private static final LocalTime AVAMISE_AEG  = LocalTime.of(11, 0);
    private static final LocalTime SULGEMISE_AEG = LocalTime.of(23, 0);

    private final BroneeringRepository broneeringRepository;
    private final RestoraniLaudRepository restoraniLaudRepository;

    public List<BroneeringDTO> getAllBookings() {
        return broneeringRepository.findAll().stream()
                .map(BroneeringDTO::from)
                .toList();
    }

    /**
     * Restoran on avatud kella 11 ja 23 vahel, broneeringu loomise funktsioon
     */
    public BroneeringDTO looBroneering(BroneeringCreateRequest req) {
        LocalTime algusKell = req.getAlgusAeg().toLocalTime();
        LocalTime loppKell  = req.getLoppAeg().toLocalTime();
        if (algusKell.isBefore(AVAMISE_AEG) || loppKell.isAfter(SULGEMISE_AEG)) { // Kui broneering on enne avamist või lõppeb pärast sulgemist
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Restoran on avatud 11:00–23:00");
        }

        RestoraniLaud laud = restoraniLaudRepository.findById(req.getLauaId()) // Kui lauda ei ole
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Lauda ei leitud: " + req.getLauaId()));

        if (!req.isKombineeritud() && laud.getMahutavus() < req.getKylalisteArv()) { // Ei kontrollita juhul, kui lauad on kombineeritud
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Laua mahutavus (%d) on väiksem kui külaliste arv (%d)"
                            .formatted(laud.getMahutavus(), req.getKylalisteArv()));
        }

        /*
        Juhul, kui laud on juba broneeritud. Ei tohiks küll võimalik olla ka frontendis seda teha, kuid igaks juhuks lisakontroll
         */
        List<Broneering> konflikt = broneeringRepository.findConflictingBookings(
                req.getLauaId(), req.getAlgusAeg(), req.getLoppAeg());
        if (!konflikt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Laud on selleks kellaajaks juba broneeritud");
        }

        Broneering broneering = Broneering.builder()
                .laud(laud)
                .kylaline(req.getKylastaja())
                .kylalisteArv(req.getKylalisteArv())
                .algusAeg(req.getAlgusAeg())
                .loppAeg(req.getLoppAeg())
                .staatus(BroneeringuStaatus.CONFIRMED)
                .kommentaar(req.getKommentaar() != null ? req.getKommentaar() : "")
                .build();

        return BroneeringDTO.from(broneeringRepository.save(broneering));
    }

    /*
    Funktsiooni tegelikult ajapuuduse tõttu frontendis ei kasutata.
     */
    public BroneeringDTO cancelBroneering(Long id) {
        Broneering broneering = broneeringRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found: " + id));
        broneering.setStaatus(BroneeringuStaatus.CANCELLED);
        return BroneeringDTO.from(broneeringRepository.save(broneering));
    }
}
