package com.agridev.repository;

import com.agridev.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductId(Long productId);

    Optional<Review> findByBuyerIdAndProductId(Long buyerId, Long productId);
}
