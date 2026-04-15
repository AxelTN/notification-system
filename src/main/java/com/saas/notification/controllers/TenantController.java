package com.saas.notification.controllers;

import com.saas.notification.dto.TenantDTO;
import com.saas.notification.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenant Management", description = "Endpoints for managing tenants")
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    @Operation(summary = "Create a new tenant")
    public ResponseEntity<TenantDTO.TenantResponse> createTenant(
            @Valid @RequestBody TenantDTO.CreateTenantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tenantService.createTenant(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tenant by ID")
    public ResponseEntity<TenantDTO.TenantResponse> getTenantById(@PathVariable Long id) {
        return ResponseEntity.ok(tenantService.getTenantById(id));
    }

    @GetMapping("/api-key/{apiKey}")
    @Operation(summary = "Get tenant by API key")
    public ResponseEntity<TenantDTO.TenantResponse> getTenantByApiKey(@PathVariable String apiKey) {
        return ResponseEntity.ok(tenantService.getTenantByApiKey(apiKey));
    }
}