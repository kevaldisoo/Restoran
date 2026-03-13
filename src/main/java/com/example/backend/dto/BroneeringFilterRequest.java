package com.example.backend.dto;

import com.example.backend.model.TsooniTyyp;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BroneeringFilterRequest {
    private LocalDate kuupaev;
    private LocalTime algusAeg;
    private LocalTime loppAeg;
    private int kylalisteArv;
    private TsooniTyyp tsoon;
    private Boolean aknaAll;
    private Boolean lastenurk;
    private Boolean privaatsus;
}
