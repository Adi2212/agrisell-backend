package com.agridev.controller;

import com.agridev.dto.PaymentRequest;
import com.agridev.dto.StripeResponse;
import com.agridev.service.PaymentService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Controller to handle payment related APIs
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // API to create Stripe checkout session
    @PostMapping("/checkout")
    public ResponseEntity<StripeResponse> checkout(
            @RequestBody PaymentRequest request) {

        StripeResponse response = paymentService.checkout(
                request.getOrderId(),
                request.getItems()
        );

        return ResponseEntity.ok(response);
    }

}
