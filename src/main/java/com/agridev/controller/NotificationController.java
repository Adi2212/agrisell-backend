package com.agridev.controller;

import com.agridev.dto.NotificationResponse;
import com.agridev.service.NotificationService;
import com.agridev.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(HttpServletRequest request) {
        Long userId = jwtUtil.extractUserId(jwtUtil.extractToken(request));
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(HttpServletRequest request) {
        Long userId = jwtUtil.extractUserId(jwtUtil.extractToken(request));
        return ResponseEntity.ok(notificationService.markAllAsRead(userId));
    }
}
