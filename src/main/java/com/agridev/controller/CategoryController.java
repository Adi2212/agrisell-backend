package com.agridev.controller;

import java.util.List;

import com.agridev.dto.CategoryDTO;
import com.agridev.dto.CreateCategoryRequest;
import com.agridev.dto.SubCategoryDTO;
import com.agridev.service.CategoryService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

// REST controller to handle all category-related API requests
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    // Fetch all main categories
    @GetMapping("/main")
    public ResponseEntity<?> getMainCategories() {
        return ResponseEntity.ok(categoryService.getMainCategories());
    }

    // Fetch sub-categories for a given parent category ID
    @GetMapping("/sub/{parentId}")
    public ResponseEntity<?> getSubCategories(
            @PathVariable @Min(value = 1, message = "Parent ID must be positive")
            Long parentId) {

        return ResponseEntity.ok(categoryService.getSubCategories(parentId));
    }

    // Add a new category (Admin only)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<?> addCategory(
            @Valid @RequestBody CreateCategoryRequest req) {

        return categoryService.addCategory(req);
    }

    // Update category (Admin only)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCategory(
            @PathVariable @Min(value = 1, message = "Category ID must be positive") Long id,
            @Valid @RequestBody CreateCategoryRequest req) {

        return categoryService.updateCategory(id, req);
    }

    // Activate / Deactivate category (Admin only)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/status/{id}/{status}")
    public ResponseEntity<?> updateCategoryStatus(
            @PathVariable @Min(value = 1, message = "Category ID must be positive") Long id,
            @PathVariable boolean status) {

        return categoryService.changeCategoryStatus(id, status);
    }

    // Retrieve all categories
    @GetMapping("/")
    public ResponseEntity<List<SubCategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getCategories());
    }

    // Retrieve category by ID
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(
            @PathVariable @Min(value = 1, message = "Category ID must be positive") Long id) {

        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }
}
