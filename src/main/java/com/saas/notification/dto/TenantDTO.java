package com.saas.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TenantDTO {

    private Long id;

    @NotBlank(message = "Tenant name is required")
    private String name;

    private String apiKey;

    private String description;

    @Email(message = "Invalid email format")
    private String adminEmail;

    // Request DTO for creating tenant
    @Data
    public static class CreateTenantRequest {
        @NotBlank(message = "Tenant name is required")
        private String name;

        private String description;

        @Email(message = "Invalid email format")
        private String adminEmail;
    }

    // Response DTO
    @Data
    public static class TenantResponse {
        private Long id;
        private String name;
        private String apiKey;
        private String description;
        private String adminEmail;
        private Integer userCount;
        private Integer notificationCount;
    }
}