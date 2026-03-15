package com.example.backend.dto;

import com.example.backend.model.RestoraniLaud;
import com.example.backend.model.TsooniTyyp;
import lombok.Data;

@Data
public class LauaSoovitusedDTO {

    private Long id;
    private String lauaNumber;
    private int mahutavus;
    private TsooniTyyp tsoon;
    private boolean aknaAll;
    private boolean lastenurk;
    private int posX;
    private int posY;
    private int width;
    private int height;

    /** True kui laud ei ole käesolevaks kellaajaks broneeritud. */
    private boolean vaba;

    /** True kui laud vastab külastaja eelistustele. */
    private boolean meetsFilter;

    /** Mida suurem skoor, seda rohkem vastab külalise eelistusele. Negatiivse või nulli skoori korral on laud kinni. */
    private int skoor;

    public static LauaSoovitusedDTO from(RestoraniLaud t, boolean vaba, boolean meetsFilter, int skoor) {
        LauaSoovitusedDTO dto = new LauaSoovitusedDTO();
        dto.setId(t.getId());
        dto.setLauaNumber(t.getLauaNumber());
        dto.setMahutavus(t.getMahutavus());
        dto.setTsoon(t.getTsoon());
        dto.setAknaAll(t.isAknaAll());
        dto.setLastenurk(t.isLastenurk());
        dto.setPosX(t.getPosX());
        dto.setPosY(t.getPosY());
        dto.setWidth(t.getWidth());
        dto.setHeight(t.getHeight());
        dto.setVaba(vaba);
        dto.setMeetsFilter(meetsFilter);
        dto.setSkoor(skoor);
        return dto;
    }
}
