package com.agridev.service;

import com.agridev.dto.CartItemDto;
import com.agridev.dto.CartSyncRequestItem;
import com.agridev.exception.UserNotFound;
import com.agridev.model.Cart;
import com.agridev.model.CartItem;
import com.agridev.model.Product;
import com.agridev.model.User;
import com.agridev.repository.CartItemRepository;
import com.agridev.repository.CartRepository;
import com.agridev.repository.ProductRepository;
import com.agridev.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public List<CartItemDto> getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return mapToDtoList(cart.getItems());
    }

    @Transactional
    public List<CartItemDto> syncCart(Long userId, List<CartSyncRequestItem> syncItems) {
        Cart cart = getOrCreateCart(userId);

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }
        
        if (syncItems != null) {
            for (CartSyncRequestItem syncItem : syncItems) {
                if (syncItem == null || syncItem.getId() == null) continue;

                Optional<CartItem> existingItem = cart.getItems().stream()
                        .filter(i -> i != null && i.getProduct() != null && syncItem.getId().equals(i.getProduct().getId()))
                        .findFirst();

                if (existingItem.isPresent()) {
                    existingItem.get().setQuantity(syncItem.getQty());
                } else {
                    productRepository.findById(syncItem.getId()).ifPresent(product -> {
                        CartItem newItem = new CartItem();
                        newItem.setCart(cart);
                        newItem.setProduct(product);
                        newItem.setQuantity(syncItem.getQty());
                        cart.getItems().add(newItem);
                    });
                }
            }
        }
        cartRepository.save(cart);
        return mapToDtoList(cart.getItems());
    }

    @Transactional
    public List<CartItemDto> addToCart(Long userId, Long productId, int qty) {
        Cart cart = getOrCreateCart(userId);

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(i -> i != null && i.getProduct() != null && productId.equals(i.getProduct().getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + qty);
        } else {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(qty);
            cart.getItems().add(newItem);
        }

        cartRepository.save(cart);
        return mapToDtoList(cart.getItems());
    }

    @Transactional
    public List<CartItemDto> updateQty(Long userId, Long productId, int qty) {
        Cart cart = getOrCreateCart(userId);
        
        if (cart.getItems() != null) {
            cart.getItems().stream()
                    .filter(i -> i != null && i.getProduct() != null && productId.equals(i.getProduct().getId()))
                    .findFirst()
                    .ifPresent(item -> item.setQuantity(qty));
        }
                
        cartRepository.save(cart);
        return mapToDtoList(cart.getItems());
    }

    @Transactional
    public List<CartItemDto> removeFromCart(Long userId, Long productId) {
        Cart cart = getOrCreateCart(userId);
        
        if (cart.getItems() != null) {
            cart.getItems().removeIf(item -> item != null && item.getProduct() != null && productId.equals(item.getProduct().getId()));
        }
        
        cartRepository.save(cart);
        return mapToDtoList(cart.getItems());
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        if (cart.getItems() != null) {
            cart.getItems().clear();
        }
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findFirstByUserIdOrderByIdDesc(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFound("User not found with id: " + userId));
            Cart newCart = new Cart();
            newCart.setUser(user);
            newCart.setItems(new ArrayList<>());
            return cartRepository.save(newCart);
        });
    }

    private List<CartItemDto> mapToDtoList(List<CartItem> items) {
        if (items == null) {
            return Collections.emptyList();
        }
        return items.stream()
                .filter(item -> item != null && item.getProduct() != null)
                .map(item -> {
                    CartItemDto dto = new CartItemDto();
                    Product p = item.getProduct();
                    dto.setId(p.getId());
                    dto.setName(p.getName());

                    double effectivePrice = p.getPrice();
                    if (p.getOffer() != null && p.getOffer().isActive()) {
                        double discount = p.getOffer().getDiscountPercentage() != null ? p.getOffer().getDiscountPercentage() : 0.0;
                        effectivePrice = Math.round((effectivePrice * (1.0 - (discount / 100.0))) * 100.0) / 100.0;
                    }

                    dto.setPrice(effectivePrice);
                    dto.setImgUrl(p.getImgUrl());
                    dto.setQty(item.getQuantity());
                    return dto;
                }).collect(Collectors.toList());
    }
}
