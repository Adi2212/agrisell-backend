package com.agridev.service;

import com.agridev.dto.*;
import com.agridev.model.Category;
import com.agridev.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final ModelMapper mapper;
    private final CategoryRepository categoryRepo;

    // Add new category (main or sub)
    public ResponseEntity<?> addCategory(CreateCategoryRequest req) {

        log.info("Add category request received: {}", req.getName());

        Category category = new Category();
        category.setName(req.getName());
        category.setImgUrl(req.getImgUrl());
        category.setActive(true);

        // Subcategory case
        if (req.getParentId() != null) {

            log.info("Creating subcategory under parentId: {}", req.getParentId());

            Category parent = categoryRepo.findByIdAndIsActiveTrue(req.getParentId())
                    .orElseThrow(() -> {
                        log.error("Parent category not found or inactive for id: {}", req.getParentId());
                        return new RuntimeException("Parent category not found or inactive");
                    });

            category.setParent(parent);

        } else {
            log.info("Creating main category");
            category.setParent(null);
        }

        Category saved = categoryRepo.save(category);

        log.info("Category created successfully with id: {}", saved.getId());

        if (req.getParentId() != null) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(mapper.map(saved, SubCategoryDTO.class));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.map(saved, MainCategoryDTO.class));
    }

    // Update category details
    public ResponseEntity<?> updateCategory(Long id, CreateCategoryRequest req) {

        log.info("Update category request received for categoryId: {}", id);

        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> {
                    log.error("Category not found with id: {}", id);
                    return new RuntimeException("Category not found");
                });

        category.setName(req.getName());
        category.setImgUrl(req.getImgUrl());

        // Update parent category if given
        if (req.getParentId() != null) {

            log.info("Updating parent category to parentId: {}", req.getParentId());

            Category parent = categoryRepo.findByIdAndIsActiveTrue(req.getParentId())
                    .orElseThrow(() -> {
                        log.error("Parent category not found or inactive for id: {}", req.getParentId());
                        return new RuntimeException("Parent category not found or inactive");
                    });

            category.setParent(parent);

        } else {
            log.info("Setting category as main category");
            category.setParent(null);
        }

        Category updated = categoryRepo.save(category);

        log.info("Category updated successfully with id: {}", updated.getId());

        return ResponseEntity.ok(mapper.map(updated, CategoryDTO.class));
    }

    // Get all categories
    public List<SubCategoryDTO> getCategories() {

        log.info("Fetching all categories");

        return categoryRepo.findAll()
                .stream()
                .map(cat -> mapper.map(cat, SubCategoryDTO.class))
                .toList();
    }

    // Get category by id (only active)
    public CategoryDTO getCategoryById(Long id) {

        log.info("Fetching category by id: {}", id);

        Category category = categoryRepo.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> {
                    log.error("Category not found or inactive with id: {}", id);
                    return new RuntimeException("Category not found or inactive");
                });

        log.info("Category fetched successfully with id: {}", id);

        return mapper.map(category, CategoryDTO.class);
    }

    // Get only active main categories
    public List<MainCategoryDTO> getMainCategories() {

        log.info("Fetching active main categories");

        return categoryRepo.findByParentIsNullAndIsActiveTrue()
                .stream()
                .map(cat -> mapper.map(cat, MainCategoryDTO.class))
                .toList();
    }

    // Get only active subcategories under a parent category
    public List<SubCategoryDTO> getSubCategories(Long parentId) {

        log.info("Fetching subcategories for parentId: {}", parentId);

        return categoryRepo.findByParentIdAndIsActiveTrue(parentId)
                .stream()
                .map(cat -> mapper.map(cat, SubCategoryDTO.class))
                .toList();
    }

    // Change category status (active/inactive)
    public ResponseEntity<?> changeCategoryStatus(Long id, boolean status) {

        log.info("Change category status request received for id: {}, newStatus: {}", id, status);

        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> {
                    log.error("Category not found with id: {}", id);
                    return new RuntimeException("Category not found");
                });

        // Prevent activating if parent is inactive
        if (status && category.getParent() != null) {

            Category parent = category.getParent();

            if (!parent.isActive()) {
                log.warn("Cannot activate category {} because parent {} is inactive", id, parent.getId());
                return ResponseEntity.badRequest()
                        .body("Cannot activate this category because parent category is inactive.");
            }
        }

        category.setActive(status);

        // If deactivating, deactivate all subcategories too
        if (!status) {
            log.info("Deactivating all subcategories under categoryId: {}", id);

            category.getSubCategory()
                    .forEach(sub -> sub.setActive(false));
        }

        categoryRepo.save(category);

        String msg = status
                ? "Category Activated Successfully"
                : "Category Deactivated Successfully";

        log.info("Category status updated successfully for id: {}", id);

        return ResponseEntity.ok(msg);
    }
}
