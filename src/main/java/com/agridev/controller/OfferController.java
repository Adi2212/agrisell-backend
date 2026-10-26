package com.agridev.controller;

import com.agridev.dto.OfferDTO;
import com.agridev.service.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;

    // Get active offers for carousel and buyer view
    @GetMapping({"", "/", "/active"})
    public ResponseEntity<List<OfferDTO>> getActiveOffers() {
        return ResponseEntity.ok(offerService.getActiveOffers());
    }

    // Get single offer by ID
    @GetMapping("/get/{id}")
    public ResponseEntity<OfferDTO> getOfferById(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.getOfferById(id));
    }
}
