package com.agridev.controller;

import com.agridev.dto.OrderRequest;
import com.agridev.dto.OrderResponse;
import com.agridev.model.Status;
import com.agridev.service.OrderService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controller to handle order related APIs
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    // Create a new order
    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest dto,
            HttpServletRequest request) {

        return ResponseEntity.ok(orderService.placeOrder(dto, request));
    }

    // Cancel Order
    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<?> cancelOrder(
            @PathVariable @Min(value = 1, message = "Order ID must be positive")
            Long orderId) {

        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }

    // Update order status
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable @Min(value = 1, message = "Order ID must be positive")
            Long id,
            @RequestParam Status status) {

        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }

    // Get logged-in user's orders
    @GetMapping("/user")
    public ResponseEntity<List<OrderResponse>> userOrders(HttpServletRequest request) {

        return ResponseEntity.ok(orderService.getUserOrders(request));
    }

    // Farmer Orders API
    @GetMapping("/farmer")
    public ResponseEntity<?> getFarmerOrders(HttpServletRequest request) {

        return ResponseEntity.ok(orderService.getOrdersForFarmer(request));
    }

    // Get single order details
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable @Min(value = 1, message = "Order ID must be positive")
            Long id) {

        return ResponseEntity.ok(orderService.getOrder(id));
    }

    // Mark payment failed
    @PutMapping("/{id}/payment-failed")
    public ResponseEntity<OrderResponse> markPaymentFailed(
            @PathVariable @Min(value = 1, message = "Order ID must be positive")
            Long id) {

        return ResponseEntity.ok(orderService.markPaymentFailed(id));
    }

    // Mark payment success
    @PutMapping("/{id}/payment-success")
    public ResponseEntity<OrderResponse> markPaymentSuccess(
            @PathVariable @Min(value = 1, message = "Order ID must be positive")
            Long id) {

        return ResponseEntity.ok(orderService.markPaymentSuccess(id));
    }
}
