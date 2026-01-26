package com.agridev.controller;

import com.agridev.dto.AddProductDTO;
import com.agridev.dto.FarmerStatsResponse;
import com.agridev.dto.ProductDTO;
import com.agridev.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Future;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/get")
    public ResponseEntity<List<ProductDTO>> getAll(){

        return ResponseEntity.ok(productService.getAllProducts());

    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ProductDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // Get products by Category ID
    @GetMapping("/get/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getByCategory(
            @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_FARMER','ROLE_ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<ProductDTO> add(@RequestBody AddProductDTO dto, HttpServletRequest request) {
        return ResponseEntity.ok(productService.addProduct(dto, request));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_FARMER','ROLE_ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<ProductDTO> update(@PathVariable Long id, @RequestBody AddProductDTO dto, HttpServletRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, dto, request));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_FARMER','ROLE_ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id, HttpServletRequest request) {
        try {
            //System.out.println("Authenticated User: " + request.getUserPrincipal());

            return ResponseEntity.ok(productService.deleteProduct(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_FARMER')")
    @GetMapping("/farmer")
    public ResponseEntity<List<ProductDTO>> getFarmer( HttpServletRequest request) {
        return ResponseEntity.ok(productService.getProductsByUserId(request));
    }

    //State for the farmer
    @PreAuthorize("hasAnyAuthority('ROLE_FARMER')")
    @GetMapping("/stats")
    public FarmerStatsResponse getStats(HttpServletRequest request) {
        return productService.getFarmerStats(request);
    }

}
