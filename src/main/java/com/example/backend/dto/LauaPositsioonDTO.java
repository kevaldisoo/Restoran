package com.example.backend.dto;

import lombok.Data;

// Abimeetod juhul, kui admin lehel laua asukohta muudetakse
@Data
public class LauaPositsioonDTO {
    private int posX;
    private int posY;
}
