package com.agridev.controller;

import com.agridev.dto.CartItemDto;
import com.agridev.dto.CartSyncRequestItem;
import com.agridev.service.CartService;
import com.agridev.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final JwtUtil jwtUtil;

    private Long getUserId(HttpServletRequest request) {
        return jwtUtil.extractUserId(jwtUtil.extractToken(request));
    }

    @GetMapping({"", "/"})
    public ResponseEntity<List<CartItemDto>> getCart(HttpServletRequest request) {
        return ResponseEntity.ok(cartService.getCart(getUserId(request)));
    }

    @PostMapping("/sync")
    public ResponseEntity<List<CartItemDto>> syncCart(@RequestBody List<CartSyncRequestItem> syncItems, HttpServletRequest request) {
        return ResponseEntity.ok(cartService.syncCart(getUserId(request), syncItems));
    }

    @PostMapping("/add")
    public ResponseEntity<List<CartItemDto>> addToCart(@RequestParam Long productId, @RequestParam(defaultValue = "1") int qty, HttpServletRequest request) {
        return ResponseEntity.ok(cartService.addToCart(getUserId(request), productId, qty));
    }

    @PutMapping("/update")
    public ResponseEntity<List<CartItemDto>> updateQty(@RequestParam Long productId, @RequestParam int qty, HttpServletRequest request) {
        return ResponseEntity.ok(cartService.updateQty(getUserId(request), productId, qty));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<List<CartItemDto>> removeFromCart(@PathVariable Long productId, HttpServletRequest request) {
        return ResponseEntity.ok(cartService.removeFromCart(getUserId(request), productId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(HttpServletRequest request) {
        cartService.clearCart(getUserId(request));
        return ResponseEntity.ok().build();
    }
}
