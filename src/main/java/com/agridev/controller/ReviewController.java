package com.agridev.controller;

import com.agridev.dto.*;
import com.agridev.service.ReviewService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // Add Review
    @PostMapping("/add/{productId}")
    public ResponseEntity<?> addReview(
            @PathVariable Long productId,
            @Valid @RequestBody AddReviewRequestDTO dto,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(reviewService.addReview(productId, dto, request));
    }

    // Get Reviews of Product
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviews(
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId));
    }
}
