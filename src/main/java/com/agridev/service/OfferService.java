package com.agridev.service;

import com.agridev.dto.OfferDTO;
import com.agridev.dto.OfferRequestDTO;
import com.agridev.dto.ProductSummaryDTO;
import com.agridev.model.Category;
import com.agridev.model.Offer;
import com.agridev.model.Product;
import com.agridev.repository.CategoryRepository;
import com.agridev.repository.OfferRepository;
import com.agridev.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferService {

    private final OfferRepository offerRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // Get active offers for Carousel / Homepage
    @Transactional(readOnly = true)
    public List<OfferDTO> getActiveOffers() {
        log.info("Fetching all active offers for carousel");
        return offerRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Get all offers for Admin management
    @Transactional(readOnly = true)
    public List<OfferDTO> getAllOffers() {
        log.info("Fetching all offers for admin");
        return offerRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Get single offer by ID
    @Transactional(readOnly = true)
    public OfferDTO getOfferById(Long id) {
        log.info("Fetching offer by id: {}", id);
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found with ID: " + id));
        return mapToDTO(offer);
    }

    // Create a new offer
    @Transactional
    public OfferDTO createOffer(OfferRequestDTO dto) {
        log.info("Creating new offer: {}", dto.getTitle());

        boolean activeStatus = dto.getIsActive();

        Offer offer = Offer.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .discountPercentage(dto.getDiscountPercentage())
                .bannerUrl(dto.getBannerUrl())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .isActive(activeStatus)
                .products(new ArrayList<>())
                .build();

        Offer savedOffer = offerRepository.save(offer);

        // Resolve product IDs (from explicit productIds and/or categoryIds)
        Set<Long> resolvedIds = resolveProductIds(dto);
        if (!resolvedIds.isEmpty()) {
            List<Product> products = productRepository.findAllById(resolvedIds);
            for (Product p : products) {
                p.setOffer(savedOffer);
            }
            productRepository.saveAll(products);
            savedOffer.setProducts(products);
        }

        return mapToDTO(savedOffer);
    }

    // Update existing offer
    @Transactional
    public OfferDTO updateOffer(Long id, OfferRequestDTO dto) {
        log.info("Updating offer id: {}", id);

        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found with ID: " + id));

        offer.setTitle(dto.getTitle());
        offer.setDescription(dto.getDescription());
        offer.setDiscountPercentage(dto.getDiscountPercentage());
        offer.setBannerUrl(dto.getBannerUrl());
        offer.setStartDate(dto.getStartDate());
        offer.setEndDate(dto.getEndDate());
        offer.setIsActive(dto.getIsActive());

        // Update product associations
        if (dto.getProductIds() != null || dto.getCategoryIds() != null) {
            Set<Long> resolvedIds = resolveProductIds(dto);

            // Unlink existing products of this offer that are no longer selected
            List<Product> currentProducts = productRepository.findAll().stream()
                    .filter(p -> p.getOffer() != null && p.getOffer().getId().equals(id))
                    .collect(Collectors.toList());

            for (Product p : currentProducts) {
                if (!resolvedIds.contains(p.getId())) {
                    p.setOffer(null);
                }
            }

            // Link new products
            List<Product> newProducts = productRepository.findAllById(resolvedIds);
            for (Product p : newProducts) {
                p.setOffer(offer);
            }

            productRepository.saveAll(currentProducts);
            productRepository.saveAll(newProducts);
            offer.setProducts(newProducts);
        }

        Offer updated = offerRepository.save(offer);
        return mapToDTO(updated);
    }

    // Toggle active status
    @Transactional
    public OfferDTO toggleStatus(Long id) {
        log.info("Toggling offer status for id: {}", id);
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found with ID: " + id));

        boolean newStatus = !offer.isActive();
        offer.setIsActive(newStatus);
        Offer saved = offerRepository.save(offer);
        return mapToDTO(saved);
    }

    // Delete offer
    @Transactional
    public void deleteOffer(Long id) {
        log.info("Deleting offer id: {}", id);
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found with ID: " + id));

        // Unlink from all products
        List<Product> products = productRepository.findAll().stream()
                .filter(p -> p.getOffer() != null && p.getOffer().getId().equals(id))
                .collect(Collectors.toList());

        for (Product p : products) {
            p.setOffer(null);
        }
        productRepository.saveAll(products);

        offerRepository.delete(offer);
    }

    // Helper method to resolve all product IDs including bulk category selections
    private Set<Long> resolveProductIds(OfferRequestDTO dto) {
        Set<Long> targetProductIds = new HashSet<>();
        if (dto.getProductIds() != null) {
            targetProductIds.addAll(dto.getProductIds());
        }

        // If categoryIds provided, automatically fetch all products in those categories & subcategories
        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            for (Long catId : dto.getCategoryIds()) {
                if (catId != null) {
                    categoryRepository.findById(catId).ifPresent(category -> {
                        // Direct products
                        List<Product> directProducts = productRepository.findByCategory(category);
                        for (Product p : directProducts) {
                            targetProductIds.add(p.getId());
                        }

                        // Subcategory products
                        List<Category> subCategories = categoryRepository.findByParentIdAndIsActiveTrue(catId);
                        for (Category sub : subCategories) {
                            List<Product> subProducts = productRepository.findByCategory(sub);
                            for (Product sp : subProducts) {
                                targetProductIds.add(sp.getId());
                            }
                        }
                    });
                }
            }
        }
        return targetProductIds;
    }

    // Helper method to convert Offer to OfferDTO
    private OfferDTO mapToDTO(Offer offer) {
        List<Product> products = offer.getProducts() != null ? offer.getProducts() : Collections.emptyList();

        List<ProductSummaryDTO> productSummaries = products.stream()
                .filter(p -> p != null)
                .map(p -> {
                    String catName = p.getCategory() != null ? p.getCategory().getName() : "";
                    String parentCatName = (p.getCategory() != null && p.getCategory().getParent() != null)
                            ? p.getCategory().getParent().getName()
                            : catName;

                    return ProductSummaryDTO.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .price(p.getPrice())
                            .imgUrl(p.getImgUrl())
                            .categoryName(catName)
                            .parentCategoryName(parentCatName)
                            .build();
                })
                .collect(Collectors.toList());

        List<Long> productIds = products.stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        return OfferDTO.builder()
                .id(offer.getId())
                .title(offer.getTitle())
                .description(offer.getDescription())
                .discountPercentage(offer.getDiscountPercentage())
                .bannerUrl(offer.getBannerUrl())
                .startDate(offer.getStartDate())
                .endDate(offer.getEndDate())
                .isActive(offer.isActive())
                .productCount(products.size())
                .productIds(productIds)
                .products(productSummaries)
                .build();
    }
}
