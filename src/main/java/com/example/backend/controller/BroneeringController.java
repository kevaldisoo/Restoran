package com.example.backend.controller;

import com.example.backend.dto.BroneeringCreateRequest;
import com.example.backend.dto.BroneeringDTO;
import com.example.backend.service.BroneeringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class BroneeringController {

    private final BroneeringService broneeringService;

    // Kõikide broneeringute näitamine
    @GetMapping
    public ResponseEntity<List<BroneeringDTO>> getAllBookings() {
        return ResponseEntity.ok(broneeringService.getAllBookings());
    }

    // Broneeringu lisamine
    @PostMapping
    public ResponseEntity<BroneeringDTO> createBooking(@RequestBody BroneeringCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(broneeringService.createBooking(req));
    }

    // Eemalda broneering
    @DeleteMapping("/{id}")
    public ResponseEntity<BroneeringDTO> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.ok(broneeringService.cancelBooking(id));
    }
}
