package com.example.backend.repository;

import com.example.backend.model.Broneering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BroneeringRepository extends JpaRepository<Broneering, Long> {

    /** Leiame kõik broneeringud, mille ajad kattuvad külalise broneeringuga [algusAeg, loppAeg). */
    @Query("""
            SELECT b FROM Broneering b
            WHERE b.staatus != 'CANCELLED'
            AND b.algusAeg < :loppAeg
            AND b.loppAeg > :algusAeg
            """)
    List<Broneering> findActiveBookingsBetween(
            @Param("algusAeg") LocalDateTime algusAeg,
            @Param("loppAeg") LocalDateTime loppAeg);

    /** Aktiivsed broneeringud, mille ajad kattuvad, et ei tekiks konflikte. */
    @Query("""
            SELECT b FROM Broneering b
            WHERE b.laud.id = :lauaId
            AND b.staatus != 'CANCELLED'
            AND b.algusAeg < :loppAeg
            AND b.loppAeg > :algusAeg
            """)
    List<Broneering> findConflictingBookings(
            @Param("lauaId") Long lauaId,
            @Param("algusAeg") LocalDateTime algusAeg,
            @Param("loppAeg") LocalDateTime loppAeg);
}
