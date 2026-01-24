package com.agridev.service;

import com.agridev.dto.*;
import com.agridev.model.Category;
import com.agridev.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final ModelMapper mapper;
    private final CategoryRepository categoryRepo;


    public ResponseEntity<?> addCategory(CreateCategoryRequest req) {

        Category category = new Category();
        category.setName(req.getName());
        category.setImgUrl(req.getImgUrl());
        category.setActive(true);


        if (req.getParentId() != null) {

            Category parent = categoryRepo.findByIdAndIsActiveTrue(req.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found or inactive"));

            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        Category saved = categoryRepo.save(category);

        if (req.getParentId() != null) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(mapper.map(saved, SubCategoryDTO.class));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.map(saved, MainCategoryDTO.class));
    }

    //Update category details
    public ResponseEntity<?> updateCategory(Long id, CreateCategoryRequest req) {

        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.setName(req.getName());

        category.setImgUrl(req.getImgUrl());

        if (req.getParentId() != null) {

            // Parent must exist and be active
            Category parent = categoryRepo.findByIdAndIsActiveTrue(req.getParentId())
                    .orElseThrow(() ->
                            new RuntimeException("Parent category not found or inactive")
                    );

            category.setParent(parent);

        } else {
            category.setParent(null); // main category
        }

        Category updated = categoryRepo.save(category);

        return ResponseEntity.ok(mapper.map(updated, CategoryDTO.class));
    }


    // Get all active categories
    public List<SubCategoryDTO> getCategories() {
        return categoryRepo.findAll()
                .stream()
                .map(cat -> mapper.map(cat, SubCategoryDTO.class))
                .toList();
    }

    //Get category by id (only active)
    public CategoryDTO getCategoryById(Long id) {

        Category category = categoryRepo.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Category not found or inactive"));

        return mapper.map(category, CategoryDTO.class);
    }

    //Get only active main categories
    public List<MainCategoryDTO> getMainCategories() {

        return categoryRepo.findByParentIsNullAndIsActiveTrue()
                .stream()
                .map(cat -> mapper.map(cat, MainCategoryDTO.class))
                .toList();
    }

    //Get only active subcategories
    public List<SubCategoryDTO> getSubCategories(Long parentId) {

        return categoryRepo.findByParentIdAndIsActiveTrue(parentId)
                .stream()
                .map(cat -> mapper.map(cat, SubCategoryDTO.class))
                .toList();
    }

    // Change category status (active / inactive)
    public ResponseEntity<?> changeCategoryStatus(Long id, boolean status) {

        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.setActive(status);

        if (!status) {
            category.getSubCategory()
                    .forEach(sub -> sub.setActive(false));
        }

        categoryRepo.save(category);

        String msg = status
                ? "Category Activated Successfully"
                : "Category Deactivated Successfully";

        return ResponseEntity.ok(msg);
    }

}
