package com.example.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BroneeringCreateRequest {
    private Long lauaId;
    private String kylastaja;
    private int kylalisteArv;
    private LocalDateTime algusAeg;
    private LocalDateTime loppAeg;
    private String kommentaar;
}
