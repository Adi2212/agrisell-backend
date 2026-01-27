package com.agridev.controller;

import com.agridev.dto.PaymentRequest;
import com.agridev.dto.StripeResponse;
import com.agridev.service.PaymentService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

// Controller to handle payment related APIs
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    // Create Stripe checkout session
    @PostMapping("/checkout")
    public ResponseEntity<StripeResponse> checkout(
            @Valid @RequestBody PaymentRequest request) {

        StripeResponse response = paymentService.checkout(
                request.getOrderId(),
                request.getItems()
        );

        return ResponseEntity.ok(response);
    }

    // Retry Payment API
    @PostMapping("/retry/{orderId}")
    public ResponseEntity<?> retryPayment(
            @PathVariable @Min(value = 1, message = "Order ID must be positive")
            Long orderId) {

        return ResponseEntity.ok(paymentService.retryPayment(orderId));
    }
}
