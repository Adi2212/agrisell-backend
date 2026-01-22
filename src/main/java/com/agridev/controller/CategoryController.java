package com.agridev.controller;

import java.util.List;


import com.agridev.dto.CategoryDTO;
import com.agridev.dto.CreateCategoryRequest;
import com.agridev.repository.CategoryRepository;
import com.agridev.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    //Retrieves all categories in DTO format.
    @GetMapping("/")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getCategories());
    }

    //Retrieves a single category by its ID.
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }
}
