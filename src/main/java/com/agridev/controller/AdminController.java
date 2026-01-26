package com.agridev.controller;

import com.agridev.dto.*;
import com.agridev.service.AdminService;
import com.agridev.service.OrderService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controller to handle admin related operations
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final OrderService orderService;

    // API to fetch paginated farmers
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/farmers")
    public Page<FarmerAdminResponse> getAllFarmers(
            @PageableDefault(size = 5) Pageable pageable) {

        return adminService.getFarmers(pageable);
    }

    // API to fetch paginated buyers
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/buyers")
    public Page<BuyerAdminResponse> getAllBuyers(
            @PageableDefault(size = 5) Pageable pageable) {

        return adminService.getBuyers(pageable);
    }

    // API to fetch paginated products
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/products")
    public Page<ProductAdminResponse> getAllProducts(
            @PageableDefault(size = 5) Pageable pageable) {

        return adminService.getProducts(pageable);
    }

    // API to fetch paginated orders
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/orders")
    public Page<OrderAdminResponse> getAllOrders(
            @PageableDefault(size = 5) Pageable pageable) {

        return adminService.getOrders(pageable);
    }

    // API to activate or block farmer account
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/farmers/{id}/status")
    public String toggleFarmer(@PathVariable Long id) {
        adminService.toggleFarmerStatus(id);
        return "Farmer status updated";
    }

    // API to block buyer account
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/buyers/{id}/status")
    public String blockBuyer(@PathVariable Long id) {
        adminService.blockBuyer(id);
        return "Buyer blocked";
    }

    // API to approve product listing
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/products/{id}/approve")
    public String approveProduct(@PathVariable Long id) {
        adminService.approveProduct(id);
        return "Product approved";
    }

    // API to delete product listing
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/products/{id}")
    public String deleteProduct(@PathVariable Long id) {
        adminService.deleteProduct(id);
        return "Product deleted";
    }

    // API to fetch order details by id
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/orders/{id}")
    public OrderDetailsAdminResponse getOrderDetails(@PathVariable Long id) {
        return adminService.getOrderDetails(id);
    }

    // API to update order delivery status
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/orders/{id}/status/{status}")
    public String updateOrderStatus(@PathVariable Long id,
                                    @PathVariable String status) {

        adminService.updateOrderStatus(id, status);
        return "Order status updated";
    }

    // API to get order status statistics for last N days
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/orders/stats/{days}")
    public List<OrderStatusStatsResponse> getOrderStats(@PathVariable Long days) {
        return orderService.getOrderStatusStats(days);
    }

    //States of the Admin page
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/stats")
    public AdminStatsResponse getStats() {
        return adminService.getDashboardStats();
    }

}
