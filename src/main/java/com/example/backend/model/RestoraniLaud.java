package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "restorani_laud")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestoraniLaud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String lauaNumber;

    @Column(nullable = false)
    private int mahutavus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TsooniTyyp tsoon;

    private boolean aknaAll;
    private boolean lastenurk;

    // SVG hall plan position
    private int posX;
    private int posY;
    private int width;
    private int height;

    /** "rect" or "circle" — circles use width as radius */
    private String shape;
}
