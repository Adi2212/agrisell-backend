package com.agridev.repository;

import com.agridev.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {

    @Query("SELECT o FROM Offer o WHERE o.isActive = true ORDER BY o.createdAt DESC")
    List<Offer> findByIsActiveTrue();

    List<Offer> findAllByOrderByCreatedAtDesc();
}
