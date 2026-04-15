package com.saas.notification.controllers;

import com.saas.notification.dto.NotificationDTO;
import com.saas.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "Endpoints for managing notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @Operation(summary = "Create and send a notification")
    public ResponseEntity<NotificationDTO.NotificationResponse> createNotification(
            @Valid @RequestBody NotificationDTO.CreateNotificationRequest request,
            @RequestHeader("X-API-Key") String apiKey) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.createNotification(request, apiKey));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all notifications for a user by ID")
    public ResponseEntity<List<NotificationDTO.NotificationResponse>> getUserNotifications(
            @PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<NotificationDTO.NotificationResponse> markAsRead(
            @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }
}