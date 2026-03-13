package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "broneering")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Broneering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "laud_id", nullable = false)
    private RestoraniLaud laud;

    @Column(nullable = false)
    private String kylaline;

    @Column(nullable = false)
    private int kylalisteArv;

    @Column(nullable = false)
    private LocalDateTime algusAeg;

    @Column(nullable = false)
    private LocalDateTime loppAeg;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BroneeringuStaatus staatus = BroneeringuStaatus.CONFIRMED;

    private String kommentaar;
}
