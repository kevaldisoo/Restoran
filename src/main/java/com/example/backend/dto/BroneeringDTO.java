package com.example.backend.dto;

import com.example.backend.model.Broneering;
import com.example.backend.model.BroneeringuStaatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BroneeringDTO {
    private Long id;
    private Long lauaId;
    private String lauaNumber;
    private String kylaline;
    private int kylalisteArv;
    private LocalDateTime algusAeg;
    private LocalDateTime loppAeg;
    private BroneeringuStaatus staatus; // Kas on kinnitatud või tühistatud staatus
    private String kommentaar;

    public static BroneeringDTO from(Broneering b) {
        BroneeringDTO dto = new BroneeringDTO();
        dto.setId(b.getId());
        dto.setLauaId(b.getLaud().getId());
        dto.setLauaNumber(b.getLaud().getLauaNumber());
        dto.setKylaline(b.getKylaline());
        dto.setKylalisteArv(b.getKylalisteArv());
        dto.setAlgusAeg(b.getAlgusAeg());
        dto.setLoppAeg(b.getLoppAeg());
        dto.setStaatus(b.getStaatus());
        dto.setKommentaar(b.getKommentaar());
        return dto;
    }
}
