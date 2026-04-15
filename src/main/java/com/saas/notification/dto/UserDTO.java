package com.saas.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDTO {

    @Data
    public static class CreateUserRequest {
        @NotBlank(message = "User name is required")
        private String name;

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        private String email;

        private String phoneNumber;

        private String role = "USER";

        // Remove @NotBlank - this will be set from header
        private String tenantApiKey;
    }

    @Data
    public static class UserResponse {
        private Long id;
        private String name;
        private String email;
        private String phoneNumber;
        private String role;
        private Long tenantId;
        private String tenantName;
    }
}