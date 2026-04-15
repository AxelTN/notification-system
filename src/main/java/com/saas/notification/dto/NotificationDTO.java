package com.saas.notification.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationDTO {

    @Data
    public static class CreateNotificationRequest {
        @NotBlank(message = "Title is required")
        private String title;

        @NotBlank(message = "Content is required")
        private String content;

        private String type = "IN_APP";

        @NotNull(message = "User ID is required")
        @Min(value = 1, message = "User ID must be positive")
        private Long userId;  // Changed from userEmail to userId
    }

    @Data
    public static class NotificationResponse {
        private Long id;
        private String title;
        private String content;
        private String type;
        private String status;
        private Long userId;      // Added userId
        private String userEmail; // Keep for convenience
        private String userName;   // Added userName
        private String createdAt;
        private String sentAt;
        private String readAt;
    }
}