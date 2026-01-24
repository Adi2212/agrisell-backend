package com.agridev.service;

import com.agridev.dto.AddProductDTO;
import com.agridev.dto.ProductDTO;
import com.agridev.exception.UserNotFound;
import com.agridev.model.Category;
import com.agridev.model.Product;
import com.agridev.model.User;
import com.agridev.repository.CategoryRepository;
import com.agridev.repository.ProductRepository;
import com.agridev.repository.UserRepository;
import com.agridev.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;
    private final ModelMapper mapper;

    // add new product
    public ProductDTO addProduct(AddProductDTO dto, HttpServletRequest request) {

        String token = jwtUtil.extractToken(request);
        Long userId = jwtUtil.extractUserId(token);

        Category category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() ->
                        new RuntimeException("Category not found with ID: " + dto.getCategoryId())
                );

        if (!category.isActive()) {
            throw new RuntimeException("Category is inactive. Cannot add product.");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFound("Invalid user id"));

        Product product = mapper.map(dto, Product.class);
        product.setUser(user);
        product.setCategory(category);

        Product saved = productRepo.save(product);

        return mapper.map(saved, ProductDTO.class);
    }


    // Update existing product
    public ProductDTO updateProduct(Long id, AddProductDTO dto, HttpServletRequest request) {
        Product product = productRepo.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        //product.setCategory(category);
        product.setImgUrl(dto.getImageUrl());
        product.setStockQuantity(dto.getStockQuantity());

        Product updated = productRepo.save(product);

        ProductDTO response = mapper.map(updated, ProductDTO.class);
        return response;
    }


     //  Delete product
    public String deleteProduct(Long id, HttpServletRequest request) {
        System.out.println("Authenticated User: ");
        String token = jwtUtil.extractToken(request);
        Long userId = jwtUtil.extractUserId(token);
        Product product = productRepo.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getUser().getId().equals(userId)) {
            productRepo.delete(product);
            return "Product deleted successfully";
        }
        throw new RuntimeException("Product not found in current user");
    }


  // Get all products
    public List<ProductDTO> getAllProducts() {
        return productRepo.findAll().stream().map(product -> {
            // Map basic fields
            return getProduct(product);
        }).collect(Collectors.toList());
    }

    private ProductDTO getProduct(Product product) {
        User user=userRepo.findById(product.getUser().getId()).orElseThrow();
        ProductDTO dto = mapper.map(product, ProductDTO.class);
        dto.setUserId(user.getId());
        dto.setAddedBy(user.getName());
        dto.setAddedByEmail(user.getEmail());
        return dto;
    }

    //  Get product by ID
    public ProductDTO getProductById(Long id) {
        Product product = productRepo.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        return getProduct(product);
    }

    public List<ProductDTO> getProductsByUserId( HttpServletRequest request) {
        String token = jwtUtil.extractToken(request);
        Long userId = jwtUtil.extractUserId(token);

        List<Product> products = productRepo.findByUserId(userId);
        List<ProductDTO> productDTOS = products.stream().map(product -> {
            return getProduct(product);
        }).collect(Collectors.toList());
        return  productDTOS;
    }

}
