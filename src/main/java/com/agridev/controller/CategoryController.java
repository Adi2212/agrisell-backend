package com.agridev.controller;

import java.util.List;


import com.agridev.dto.CategoryDTO;
import com.agridev.dto.CreateCategoryRequest;
import com.agridev.dto.SubCategoryDTO;
import com.agridev.repository.CategoryRepository;
import com.agridev.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

//REST controller to handle all category-related API requests.
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    //Fetches all main (parent) categories.
    @GetMapping("/main")
    public ResponseEntity<?> getMainCategories() {
        return ResponseEntity.ok(categoryService.getMainCategories());
    }

    //Fetches sub-categories for a given parent category ID.
    @GetMapping("/sub/{parentId}")
    public ResponseEntity<?> getSubCategories(@PathVariable Long parentId) {
        return ResponseEntity.ok(categoryService.getSubCategories(parentId));
    }

    //Adds a new category (accessible only to admin users).
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<?> addCategory(@RequestBody CreateCategoryRequest req) {
        return categoryService.addCategory(req);
    }

    //Update category (Admin only)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCategory(
            @PathVariable Long id,
            @RequestBody CreateCategoryRequest req
    ) {
        return categoryService.updateCategory(id, req);
    }

    //Soft deletes category (accessible only to admin users).
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/status/{id}/{status}")
    public ResponseEntity<?> updateCategoryStatus(
            @PathVariable Long id,
            @PathVariable boolean status
    ) {
        return categoryService.changeCategoryStatus(id, status);
    }


    //Retrieves all categories in DTO format.
    @GetMapping("/")
    public ResponseEntity<List<SubCategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getCategories());
    }

    //Retrieves a single category by its ID.
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }
}
