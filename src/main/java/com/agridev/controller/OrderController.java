package com.agridev.controller;

import com.agridev.dto.OrderRequest;
import com.agridev.dto.OrderResponse;
import com.agridev.model.Status;
import com.agridev.service.OrderService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controller to handle order related APIs
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // API to create a new order
    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody OrderRequest dto,
            HttpServletRequest request) {

        return ResponseEntity.ok(orderService.placeOrder(dto, request));
    }

    // API to update order status by admin or seller
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Status status) {

        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }

    // API to get all orders of logged-in user
    @GetMapping("/user")
    public ResponseEntity<List<OrderResponse>> userOrders(HttpServletRequest request) {

        return ResponseEntity.ok(orderService.getUserOrders(request));
    }

    // API to get single order details by id
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {

        return ResponseEntity.ok(orderService.getOrder(id));
    }

    // API to mark payment failed and cancel order
    @PutMapping("/{id}/payment-failed")
    public ResponseEntity<OrderResponse> markPaymentFailed(@PathVariable Long id) {

        return ResponseEntity.ok(orderService.markPaymentFailed(id));
    }

    // API to mark payment success after confirmation
    @PutMapping("/{id}/payment-success")
    public ResponseEntity<OrderResponse> markPaymentSuccess(@PathVariable Long id) {

        return ResponseEntity.ok(orderService.markPaymentSuccess(id));
    }

}
