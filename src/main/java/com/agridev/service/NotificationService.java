package com.agridev.service;

import com.agridev.dto.NotificationResponse;
import com.agridev.model.Notification;
import com.agridev.model.User;
import com.agridev.repository.NotificationRepository;
import com.agridev.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public void createNotification(Long userId, String title, String message) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.warn("Cannot create notification, user not found for ID: {}", userId);
                return;
            }

            Notification n = new Notification();
            n.setUser(user);
            n.setTitle(title);
            n.setMessage(message);
            n.setRead(false);
            
            notificationRepository.save(n);
            log.info("Notification created for user {}: {}", userId, title);
        } catch (Exception e) {
            log.error("Failed to create notification for user {}", userId, e);
        }
    }

    public List<NotificationResponse> getUserNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream().map(n -> {
            NotificationResponse res = new NotificationResponse();
            res.setId(n.getId());
            res.setTitle(n.getTitle());
            res.setMessage(n.getMessage());
            res.setRead(n.isRead());
            res.setCreatedAt(n.getCreatedAt());
            return res;
        }).collect(Collectors.toList());
    }

    public Map<String, String> markAsRead(Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        n.setRead(true);
        notificationRepository.save(n);
        log.info("Notification {} marked as read", notificationId);
        return Map.of("message", "Marked as read");
    }

    public Map<String, String> markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalse(userId);
        for(Notification n : unread) {
            n.setRead(true);
        }
        notificationRepository.saveAll(unread);
        log.info("All unread notifications for user {} marked as read", userId);
        return Map.of("message", "All marked as read");
    }
}
