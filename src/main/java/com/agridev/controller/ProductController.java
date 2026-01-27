package com.agridev.controller;

import com.agridev.dto.AddProductDTO;
import com.agridev.dto.FarmerStatsResponse;
import com.agridev.dto.ProductDTO;
import com.agridev.service.ProductService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    // Get all products
    @GetMapping("/get")
    public ResponseEntity<List<ProductDTO>> getAll() {

        return ResponseEntity.ok(productService.getAllProducts());
    }

    // Get product by ID
    @GetMapping("/get/{id}")
    public ResponseEntity<ProductDTO> getById(
            @PathVariable @Min(value = 1, message = "Product ID must be positive") Long id) {

        return ResponseEntity.ok(productService.getProductById(id));
    }

    // Get products by Category ID
    @GetMapping("/get/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getByCategory(
            @PathVariable @Min(value = 1, message = "Category ID must be positive") Long categoryId) {

        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    // Add new product (Farmer/Admin)
    @PreAuthorize("hasAnyAuthority('ROLE_FARMER','ROLE_ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<ProductDTO> add(
            @Valid @RequestBody AddProductDTO dto,
            HttpServletRequest request) {

        return ResponseEntity.ok(productService.addProduct(dto, request));
    }

    // Update existing product (Farmer/Admin)
    @PreAuthorize("hasAnyAuthority('ROLE_FARMER','ROLE_ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<ProductDTO> update(
            @PathVariable @Min(value = 1, message = "Product ID must be positive") Long id,
            @Valid @RequestBody AddProductDTO dto,
            HttpServletRequest request) {

        return ResponseEntity.ok(productService.updateProduct(id, dto, request));
    }

    // Delete product (Farmer/Admin)
    @PreAuthorize("hasAnyAuthority('ROLE_FARMER','ROLE_ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(
            @PathVariable @Min(value = 1, message = "Product ID must be positive") Long id,
            HttpServletRequest request) {

        return ResponseEntity.ok(productService.deleteProduct(id, request));
    }

    // Get logged-in farmer products
    @PreAuthorize("hasAuthority('ROLE_FARMER')")
    @GetMapping("/farmer")
    public ResponseEntity<List<ProductDTO>> getFarmerProducts(HttpServletRequest request) {

        return ResponseEntity.ok(productService.getProductsByUserId(request));
    }

    // Farmer Dashboard Stats
    @PreAuthorize("hasAuthority('ROLE_FARMER')")
    @GetMapping("/stats")
    public ResponseEntity<FarmerStatsResponse> getStats(HttpServletRequest request) {

        return ResponseEntity.ok(productService.getFarmerStats(request));
    }
}
