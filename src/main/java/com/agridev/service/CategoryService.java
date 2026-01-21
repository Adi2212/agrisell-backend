package com.agridev.service;

import com.agridev.dto.CategoryDTO;
import com.agridev.dto.CreateCategoryRequest;
import com.agridev.dto.MainCategoryDTO;
import com.agridev.dto.SubCategoryDTO;
import com.agridev.model.Category;
import com.agridev.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final ModelMapper mapper;
    private final CategoryRepository categoryRepo;

    public ResponseEntity<?> addCategory(CreateCategoryRequest req) {

        Category category = new Category();
        category.setName(req.getName());
        category.setImgUrl(req.getImageUrl());

        //  If parentId = null -> MAIN CATEGORY
        if (req.getParentId() != null) {
            Category parent = categoryRepo.findById(req.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            category.setParent(parent);
        } else {
            category.setParent(null); // Main Category
        }

        Category saved = categoryRepo.save(category);
        if (req.getParentId() != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(mapper.map(saved, SubCategoryDTO.class));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.map(saved, MainCategoryDTO.class));
    }



    public List<CategoryDTO> getCategories() {
        return categoryRepo.findAll()
                .stream()
                .map(category -> mapper.map(category, CategoryDTO.class))
                .collect(Collectors.toList());
    }

    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));
        return mapper.map(category, CategoryDTO.class);
    }

    public List<MainCategoryDTO> getMainCategories() {
        return categoryRepo.findByParentIsNull()
                .stream()
                .map(category -> mapper.map(category, MainCategoryDTO.class))
                .toList();
    }

    public List<SubCategoryDTO> getSubCategories(Long parentId) {
        return categoryRepo.findByParentId(parentId)
                .stream()
                .map(category -> mapper.map(category, SubCategoryDTO.class))
                .toList();
    }
}
