package com.agridev.service;

import com.agridev.dto.AddReviewRequestDTO;
import com.agridev.dto.ReviewResponseDTO;
import com.agridev.model.*;
import com.agridev.repository.*;

import com.agridev.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;
    private final UserRepository userRepo;

    private final JwtUtil jwtUtil;
    private final ModelMapper mapper;

    // Add Review
    public ReviewResponseDTO addReview(
            Long productId,
            AddReviewRequestDTO dto,
            HttpServletRequest request
    ) {

        Long buyerId = jwtUtil.extractUserId(jwtUtil.extractToken(request));

        log.info("Review request received from buyerId: {} for productId: {}", buyerId, productId);

        User buyer = userRepo.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check delivered order exists
        boolean purchased = orderRepo.hasDeliveredOrder(buyerId, productId);

        if (!purchased) {
            throw new RuntimeException("You can review only after product delivery");
        }

        // Prevent duplicate review
        if (reviewRepo.findByBuyerIdAndProductId(buyerId, productId).isPresent()) {
            throw new RuntimeException("You already reviewed this product");
        }

        Review review = Review.builder()
                .rating(dto.getRating())
                .comment(dto.getComment())
                .buyer(buyer)
                .product(product)
                .createdAt(LocalDateTime.now())
                .build();

        Review saved = reviewRepo.save(review);

        log.info("Review saved successfully with id: {}", saved.getId());

        ReviewResponseDTO response = mapper.map(saved, ReviewResponseDTO.class);
        response.setBuyerName(buyer.getName());

        return response;
    }

    // Get All Reviews of Product
    public List<ReviewResponseDTO> getProductReviews(Long productId) {

        return reviewRepo.findByProductId(productId)
                .stream()
                .map(review -> {
                    ReviewResponseDTO dto =
                            mapper.map(review, ReviewResponseDTO.class);

                    dto.setBuyerName(review.getBuyer().getName());
                    return dto;
                })
                .toList();
    }
}
