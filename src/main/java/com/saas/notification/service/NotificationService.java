package com.saas.notification.service;

import com.saas.notification.controllers.WebSocketController;
import com.saas.notification.dto.NotificationDTO;
import com.saas.notification.domain.entity.Notification;
import com.saas.notification.domain.entity.Tenant;
import com.saas.notification.domain.entity.User;
import com.saas.notification.domain.enums.NotificationStatus;
import com.saas.notification.domain.enums.NotificationType;
import com.saas.notification.exception.ResourceNotFoundException;
import com.saas.notification.repository.NotificationRepository;
import com.saas.notification.repository.TenantRepository;
import com.saas.notification.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final WebSocketController webSocketController;

    @Transactional
    public NotificationDTO.NotificationResponse createNotification(
            NotificationDTO.CreateNotificationRequest request, String apiKey) {

        log.info("Creating notification: {}", request.getTitle());

        // Validate tenant
        Tenant tenant = tenantRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid tenant API key"));

        // Find user by ID
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        // Verify user belongs to this tenant (security check)
        if (!user.getTenant().getId().equals(tenant.getId())) {
            throw new RuntimeException("User does not belong to this tenant");
        }

        // Create notification
        Notification notification = new Notification();
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setType(NotificationType.valueOf(request.getType()));
        notification.setStatus(NotificationStatus.PENDING);
        notification.setTenant(tenant);
        notification.setUser(user);

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification created with ID: {}", savedNotification.getId());

        // Send notification (simulate email/SMS)
        sendNotification(savedNotification);

        // Send real-time WebSocket notification
        sendWebSocketNotification(savedNotification);

        return mapToResponse(savedNotification);
    }

    @Transactional
    public NotificationDTO.NotificationResponse markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));

        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());

        Notification updatedNotification = notificationRepository.save(notification);
        log.info("Notification {} marked as read", notificationId);

        // Send WebSocket update for read status
        sendWebSocketNotification(updatedNotification);

        return mapToResponse(updatedNotification);
    }

    public List<NotificationDTO.NotificationResponse> getUserNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return notificationRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO.NotificationResponse> getUnreadNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return notificationRepository.findByUserIdAndStatus(user.getId(), "PENDING")
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return (long) notificationRepository.findByUserIdAndStatus(user.getId(), "PENDING").size();
    }

    private void sendNotification(Notification notification) {
        // Simulate sending email/SMS/push notification
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());
        notificationRepository.save(notification);

        log.info("Notification sent to user: {} ({})",
                notification.getUser().getName(),
                notification.getUser().getEmail());

        // In real implementation, you would:
        // - Send email via JavaMailSender
        // - Send SMS via Twilio
        // - Send push notification via Firebase
    }

    private void sendWebSocketNotification(Notification notification) {
        try {
            NotificationDTO.NotificationResponse response = mapToResponse(notification);

            // Send to specific user
            webSocketController.sendToUser(notification.getUser().getId(), response);

            log.info("WebSocket notification sent to user: {}", notification.getUser().getId());
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification: {}", e.getMessage());
        }
    }

    private NotificationDTO.NotificationResponse mapToResponse(Notification notification) {
        NotificationDTO.NotificationResponse response = new NotificationDTO.NotificationResponse();
        response.setId(notification.getId());
        response.setTitle(notification.getTitle());
        response.setContent(notification.getContent());
        response.setType(notification.getType() != null ? notification.getType().toString() : null);
        response.setStatus(notification.getStatus() != null ? notification.getStatus().toString() : null);
        response.setUserId(notification.getUser().getId());
        response.setUserEmail(notification.getUser().getEmail());
        response.setUserName(notification.getUser().getName());
        response.setCreatedAt(formatDateTime(notification.getCreatedAt()));
        response.setSentAt(formatDateTime(notification.getSentAt()));
        response.setReadAt(formatDateTime(notification.getReadAt()));
        return response;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}