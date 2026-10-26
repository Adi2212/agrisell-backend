package com.agridev.controller;

import com.agridev.dto.WishlistItemDto;
import com.agridev.service.WishlistService;
import com.agridev.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;
    private final JwtUtil jwtUtil;

    private Long getUserId(HttpServletRequest request) {
        return jwtUtil.extractUserId(jwtUtil.extractToken(request));
    }

    @GetMapping({"", "/"})
    public ResponseEntity<List<WishlistItemDto>> getWishlist(HttpServletRequest request) {
        return ResponseEntity.ok(wishlistService.getWishlist(getUserId(request)));
    }

    @PostMapping("/add")
    public ResponseEntity<List<WishlistItemDto>> addToWishlist(@RequestParam Long productId, HttpServletRequest request) {
        return ResponseEntity.ok(wishlistService.addToWishlist(getUserId(request), productId));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<List<WishlistItemDto>> removeFromWishlist(@PathVariable Long productId, HttpServletRequest request) {
        return ResponseEntity.ok(wishlistService.removeFromWishlist(getUserId(request), productId));
    }

    @PostMapping("/move-to-cart")
    public ResponseEntity<Void> moveToCart(@RequestParam Long productId, HttpServletRequest request) {
        wishlistService.moveToCart(getUserId(request), productId);
        return ResponseEntity.ok().build();
    }
}
