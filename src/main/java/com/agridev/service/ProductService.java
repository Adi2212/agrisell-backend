package com.agridev.service;

import com.agridev.dto.AddProductDTO;
import com.agridev.dto.FarmerStatsResponse;
import com.agridev.dto.OfferSummaryDTO;
import com.agridev.dto.ParentCategoryDTO;
import com.agridev.dto.ProductDTO;
import com.agridev.exception.UserNotFound;
import com.agridev.model.Category;
import com.agridev.model.Offer;
import com.agridev.model.Product;
import com.agridev.model.Status;
import com.agridev.model.User;
import com.agridev.repository.CategoryRepository;
import com.agridev.repository.OrderRepository;
import com.agridev.repository.ProductRepository;
import com.agridev.repository.UserRepository;
import com.agridev.utils.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ModelMapper mapper;

    // Add new product
    public ProductDTO addProduct(AddProductDTO dto, HttpServletRequest request) {

        Long userId = jwtUtil.extractUserId(jwtUtil.extractToken(request));

        log.info("Add product request received from userId: {}", userId);

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> {
                    log.error("Category not found with id: {}", dto.getCategoryId());
                    return new RuntimeException(
                            "Category not found with ID: " + dto.getCategoryId()
                    );
                });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", userId);
                    return new UserNotFound("Invalid user id");
                });

        Product product = mapper.map(dto, Product.class);
        product.setUser(user);
        product.setCategory(category);

        Product saved = productRepository.save(product);

        log.info("Product added successfully with productId: {}", saved.getId());

        return getProduct(saved);
    }

    // Update existing product
    public ProductDTO updateProduct(Long id, AddProductDTO dto, HttpServletRequest request) {

        Long userId = jwtUtil.extractUserId(jwtUtil.extractToken(request));
        log.info("Update product request received for productId: {} by userId: {}", id, userId);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product not found with id: {}", id);
                    return new RuntimeException("Product not found");
                });

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setImgUrl(dto.getImgUrl());
        product.setStockQuantity(dto.getStockQuantity());

        Product updated = productRepository.save(product);

        log.info("Product updated successfully with productId: {}", updated.getId());

        return getProduct(updated);
    }

    // Delete product
    public String deleteProduct(Long id, HttpServletRequest request) {

        Long userId = jwtUtil.extractUserId(jwtUtil.extractToken(request));
        log.info("Delete product request received for productId: {} by userId: {}", id, userId);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product not found with id: {}", id);
                    return new RuntimeException("Product not found");
                });

        productRepository.delete(product);

        log.info("Product deleted successfully with productId: {}", id);

        return "Product deleted successfully";
    }

    // Get all products
    public List<ProductDTO> getAllProducts() {

        log.info("Fetching all products");

        List<Product> products = productRepository.findAll();

        return products
                .stream()
                .map(this::getProduct)
                .collect(Collectors.toList());
    }

    // Get product by ID
    public ProductDTO getProductById(Long id) {

        log.info("Fetching product details for productId: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product not found with id: {}", id);
                    return new RuntimeException("Product not found");
                });

        return getProduct(product);
    }

    // Get products under category
    public List<ProductDTO> getProductsByCategory(Long categoryId) {

        log.info("Fetching products for categoryId: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.error("Category not found with id: {}", categoryId);
                    return new RuntimeException("Category not found");
                });

        return productRepository.findByCategory(category)
                .stream()
                .map(this::getProduct)
                .collect(Collectors.toList());
    }

    // Get products of logged-in farmer
    public List<ProductDTO> getProductsByFarmer(HttpServletRequest request) {

        Long farmerId = jwtUtil.extractUserId(jwtUtil.extractToken(request));

        log.info("Fetching products for farmerId: {}", farmerId);

        return productRepository.findByUserId(farmerId)
                .stream()
                .map(this::getProduct)
                .collect(Collectors.toList());
    }

    // Get products of logged-in user / farmer (controller alias)
    public List<ProductDTO> getProductsByUserId(HttpServletRequest request) {
        return getProductsByFarmer(request);
    }

    // Fetch farmer stats (total products, orders)
    public FarmerStatsResponse getFarmerStats(HttpServletRequest request) {

        Long farmerId = jwtUtil.extractUserId(jwtUtil.extractToken(request));

        log.info("Calculating stats for farmerId: {}", farmerId);

        int totalProducts = productRepository.countByUserId(farmerId);
        long pendingOrders =
                orderRepository.countOrdersByFarmerAndStatus(farmerId, Status.PENDING);

        long completedOrders =
                orderRepository.countOrdersByFarmerAndStatus(farmerId, Status.DELIVERED);

        log.info("Farmer stats calculated successfully for farmerId: {}", farmerId);

        return new FarmerStatsResponse(totalProducts, pendingOrders, completedOrders);
    }

    // Helper method to map Product entity to ProductDTO with offer, category hierarchy, and discount details
    private ProductDTO getProduct(Product product) {

        ProductDTO dto = mapper.map(product, ProductDTO.class);

        // Populate Category and Parent Category for precise category-based filtering and offers
        if (product.getCategory() != null) {
            Category cat = product.getCategory();
            ParentCategoryDTO directCat = new ParentCategoryDTO(cat.getId(), cat.getName());
            dto.setCategory(directCat);

            if (cat.getParent() != null) {
                Category parentCat = cat.getParent();
                dto.setParentCategory(new ParentCategoryDTO(parentCat.getId(), parentCat.getName()));
            } else {
                dto.setParentCategory(directCat);
            }
        }

        if (product.getOffer() != null && product.getOffer().isActive()) {
            Offer offer = product.getOffer();
            OfferSummaryDTO offerDTO = OfferSummaryDTO.builder()
                    .id(offer.getId())
                    .title(offer.getTitle())
                    .description(offer.getDescription())
                    .discountPercentage(offer.getDiscountPercentage())
                    .bannerUrl(offer.getBannerUrl())
                    .build();

            dto.setOffer(offerDTO);

            double discount = offer.getDiscountPercentage() != null ? offer.getDiscountPercentage() : 0.0;
            double discounted = Math.round((product.getPrice() * (1.0 - (discount / 100.0))) * 100.0) / 100.0;
            dto.setDiscountedPrice(discounted);
        } else {
            dto.setDiscountedPrice(product.getPrice());
        }

        return dto;
    }
}
