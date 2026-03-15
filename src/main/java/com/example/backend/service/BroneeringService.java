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

import java.util.List;

@Service
@RequiredArgsConstructor
public class BroneeringService {

    private final BroneeringRepository broneeringRepository;
    private final RestoraniLaudRepository restoraniLaudRepository;

    public List<BroneeringDTO> getAllBookings() {
        return broneeringRepository.findAll().stream()
                .map(BroneeringDTO::from)
                .toList();
    }

    public BroneeringDTO createBooking(BroneeringCreateRequest req) {
        RestoraniLaud laud = restoraniLaudRepository.findById(req.getLauaId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Table not found: " + req.getLauaId()));

        if (!req.isKombineeritud() && laud.getMahutavus() < req.getKylalisteArv()) { // Ei kontrollita juhul, kui lauad on kombineeritud
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Laua mahutavus (%d) on väiksem kui külaliste arv (%d)"
                            .formatted(laud.getMahutavus(), req.getKylalisteArv()));
        }

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

    public BroneeringDTO cancelBooking(Long id) {
        Broneering broneering = broneeringRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found: " + id));
        broneering.setStaatus(BroneeringuStaatus.CANCELLED);
        return BroneeringDTO.from(broneeringRepository.save(broneering));
    }
}
