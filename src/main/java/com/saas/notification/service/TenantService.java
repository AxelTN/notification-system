package com.saas.notification.service;

import com.saas.notification.dto.TenantDTO;
import com.saas.notification.domain.entity.Tenant;
import com.saas.notification.exception.ResourceNotFoundException;
import com.saas.notification.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {

    private final TenantRepository tenantRepository;

    @Transactional
    public TenantDTO.TenantResponse createTenant(TenantDTO.CreateTenantRequest request) {
        log.info("Creating new tenant: {}", request.getName());

        // Check if tenant exists
        if (tenantRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tenant with name " + request.getName() + " already exists");
        }

        // Create tenant
        Tenant tenant = new Tenant();
        tenant.setName(request.getName());
        tenant.setDescription(request.getDescription());
        tenant.setAdminEmail(request.getAdminEmail());
        tenant.setApiKey(generateApiKey());

        Tenant savedTenant = tenantRepository.save(tenant);
        log.info("Tenant created successfully with ID: {}", savedTenant.getId());

        return mapToResponse(savedTenant);
    }

    public TenantDTO.TenantResponse getTenantById(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with ID: " + id));
        return mapToResponse(tenant);
    }

    public TenantDTO.TenantResponse getTenantByApiKey(String apiKey) {
        Tenant tenant = tenantRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid API Key"));
        return mapToResponse(tenant);
    }

    private String generateApiKey() {
        return "tk_" + UUID.randomUUID().toString().replace("-", "");
    }

    private TenantDTO.TenantResponse mapToResponse(Tenant tenant) {
        TenantDTO.TenantResponse response = new TenantDTO.TenantResponse();
        response.setId(tenant.getId());
        response.setName(tenant.getName());
        response.setApiKey(tenant.getApiKey());
        response.setDescription(tenant.getDescription());
        response.setAdminEmail(tenant.getAdminEmail());
        response.setUserCount(tenant.getUsers() != null ? tenant.getUsers().size() : 0);
        response.setNotificationCount(tenant.getNotifications() != null ? tenant.getNotifications().size() : 0);
        return response;
    }
}