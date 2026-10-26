package com.agridev.service;

import com.agridev.dto.WishlistItemDto;
import com.agridev.exception.UserNotFound;
import com.agridev.model.Product;
import com.agridev.model.User;
import com.agridev.model.Wishlist;
import com.agridev.model.WishlistItem;
import com.agridev.repository.ProductRepository;
import com.agridev.repository.UserRepository;
import com.agridev.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    @Transactional
    public List<WishlistItemDto> getWishlist(Long userId) {
        Wishlist wishlist = getOrCreateWishlist(userId);
        return mapToDtoList(wishlist.getItems());
    }

    @Transactional
    public List<WishlistItemDto> addToWishlist(Long userId, Long productId) {
        Wishlist wishlist = getOrCreateWishlist(userId);

        if (wishlist.getItems() == null) {
            wishlist.setItems(new ArrayList<>());
        }

        boolean exists = wishlist.getItems().stream()
                .filter(i -> i != null && i.getProduct() != null)
                .anyMatch(i -> productId.equals(i.getProduct().getId()));

        if (!exists) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            WishlistItem item = new WishlistItem();
            item.setWishlist(wishlist);
            item.setProduct(product);
            wishlist.getItems().add(item);
            wishlistRepository.save(wishlist);
        }

        return mapToDtoList(wishlist.getItems());
    }

    @Transactional
    public List<WishlistItemDto> removeFromWishlist(Long userId, Long productId) {
        Wishlist wishlist = getOrCreateWishlist(userId);
        if (wishlist.getItems() != null) {
            wishlist.getItems().removeIf(item -> item != null && item.getProduct() != null && productId.equals(item.getProduct().getId()));
        }
        wishlistRepository.save(wishlist);
        return mapToDtoList(wishlist.getItems());
    }

    @Transactional
    public void moveToCart(Long userId, Long productId) {
        removeFromWishlist(userId, productId);
        cartService.addToCart(userId, productId, 1);
    }

    private Wishlist getOrCreateWishlist(Long userId) {
        return wishlistRepository.findFirstByUserIdOrderByIdDesc(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFound("User not found with id: " + userId));
            Wishlist newWishlist = new Wishlist();
            newWishlist.setUser(user);
            newWishlist.setItems(new ArrayList<>());
            return wishlistRepository.save(newWishlist);
        });
    }

    private List<WishlistItemDto> mapToDtoList(List<WishlistItem> items) {
        if (items == null) {
            return Collections.emptyList();
        }
        return items.stream()
                .filter(item -> item != null && item.getProduct() != null)
                .map(item -> {
                    WishlistItemDto dto = new WishlistItemDto();
                    dto.setId(item.getProduct().getId());
                    dto.setName(item.getProduct().getName());
                    dto.setPrice(item.getProduct().getPrice());
                    dto.setImgUrl(item.getProduct().getImgUrl());
                    return dto;
                }).collect(Collectors.toList());
    }
}
