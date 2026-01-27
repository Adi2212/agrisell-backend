package com.agridev.controller;

import com.agridev.dto.*;
import com.agridev.service.AdminService;
import com.agridev.service.OrderService;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controller to handle admin related operations
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Validated
public class AdminController {

    private final AdminService adminService;
    private final OrderService orderService;

    // Fetch paginated farmers
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/farmers")
    public Page<FarmerAdminResponse> getAllFarmers(
            @PageableDefault(size = 5) Pageable pageable) {

        return adminService.getFarmers(pageable);
    }

    // Fetch paginated buyers
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/buyers")
    public Page<BuyerAdminResponse> getAllBuyers(
            @PageableDefault(size = 5) Pageable pageable) {

        return adminService.getBuyers(pageable);
    }

    // Fetch paginated products
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/products")
    public Page<ProductAdminResponse> getAllProducts(
            @PageableDefault(size = 5) Pageable pageable) {

        return adminService.getProducts(pageable);
    }

    // Fetch paginated orders
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/orders")
    public Page<OrderAdminResponse> getAllOrders(
            @PageableDefault(size = 5) Pageable pageable) {

        return adminService.getOrders(pageable);
    }

    // Activate/Block farmer account
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/farmers/{id}/status")
    public String toggleFarmer(
            @PathVariable @Min(value = 1, message = "Farmer ID must be positive") Long id) {

        adminService.toggleFarmerStatus(id);
        return "Farmer status updated";
    }

    // Block buyer account
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/buyers/{id}/status")
    public String blockBuyer(
            @PathVariable @Min(value = 1, message = "Buyer ID must be positive") Long id) {

        adminService.blockBuyer(id);
        return "Buyer blocked";
    }

    // Approve product listing
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/products/{id}/approve")
    public String approveProduct(
            @PathVariable @Min(value = 1, message = "Product ID must be positive") Long id) {

        adminService.approveProduct(id);
        return "Product approved";
    }

    // Delete product listing
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/products/{id}")
    public String deleteProduct(
            @PathVariable @Min(value = 1, message = "Product ID must be positive") Long id) {

        adminService.deleteProduct(id);
        return "Product deleted";
    }

    // Fetch order details by id
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/orders/{id}")
    public OrderDetailsAdminResponse getOrderDetails(
            @PathVariable @Min(value = 1, message = "Order ID must be positive") Long id) {

        return adminService.getOrderDetails(id);
    }

    // Update order delivery status
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/orders/{id}/status/{status}")
    public String updateOrderStatus(
            @PathVariable @Min(value = 1, message = "Order ID must be positive") Long id,
            @PathVariable @NotBlank(message = "Status is required") String status) {

        adminService.updateOrderStatus(id, status);
        return "Order status updated";
    }

    // Get order stats for last N days
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/orders/stats/{days}")
    public List<OrderStatusStatsResponse> getOrderStats(
            @PathVariable @Min(value = 1, message = "Days must be at least 1") Long days) {

        return orderService.getOrderStatusStats(days);
    }

    // Dashboard stats
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/stats")
    public AdminStatsResponse getStats() {

        return adminService.getDashboardStats();
    }
}
