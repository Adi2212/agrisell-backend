package com.agridev.controller;

import com.agridev.dto.OfferDTO;
import com.agridev.dto.OfferRequestDTO;
import com.agridev.service.OfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/offers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOfferController {

    private final OfferService offerService;

    // Get all offers for admin
    @GetMapping({"", "/"})
    public ResponseEntity<List<OfferDTO>> getAllOffers() {
        return ResponseEntity.ok(offerService.getAllOffers());
    }

    // Get single offer by ID
    @GetMapping("/{id}")
    public ResponseEntity<OfferDTO> getOfferById(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.getOfferById(id));
    }

    // Create new offer
    @PostMapping
    public ResponseEntity<OfferDTO> createOffer(@Valid @RequestBody OfferRequestDTO dto) {
        return ResponseEntity.ok(offerService.createOffer(dto));
    }

    // Update existing offer
    @PutMapping("/{id}")
    public ResponseEntity<OfferDTO> updateOffer(@PathVariable Long id, @Valid @RequestBody OfferRequestDTO dto) {
        return ResponseEntity.ok(offerService.updateOffer(id, dto));
    }

    // Toggle active status
    @PatchMapping("/{id}/status")
    public ResponseEntity<OfferDTO> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.toggleStatus(id));
    }

    // Delete offer
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        offerService.deleteOffer(id);
        return ResponseEntity.ok().build();
    }
}
