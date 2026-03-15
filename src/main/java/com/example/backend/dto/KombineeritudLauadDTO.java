package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KombineeritudLauadDTO {
    private LauaSoovitusedDTO laud1;
    private LauaSoovitusedDTO laud2;
    private int kombineeritudMahutavus;
    private int skoor;
}
