package com.saas.notification.service;

import com.saas.notification.dto.UserDTO;
import com.saas.notification.domain.entity.Tenant;
import com.saas.notification.domain.entity.User;
import com.saas.notification.domain.enums.UserRole;
import com.saas.notification.exception.ResourceNotFoundException;
import com.saas.notification.repository.TenantRepository;
import com.saas.notification.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    @Transactional
    public UserDTO.UserResponse createUser(UserDTO.CreateUserRequest request) {
        log.info("Creating new user: {}", request.getEmail());
        log.info("Request role: {}", request.getRole());
        log.info("Tenant API key: {}", request.getTenantApiKey());

        // Validate tenant by API key
        Tenant tenant = tenantRepository.findByApiKey(request.getTenantApiKey())
                .orElseThrow(() -> {
                    log.error("Tenant not found for API key: {}", request.getTenantApiKey());
                    return new ResourceNotFoundException("Invalid tenant API key");
                });

        log.info("Found tenant: {} with ID: {}", tenant.getName(), tenant.getId());

        // Check if user exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with email " + request.getEmail() + " already exists");
        }

        // Create user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        // Convert String role to Enum
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            try {
                user.setRole(UserRole.valueOf(request.getRole().toUpperCase()));
                log.info("Set role to: {}", user.getRole());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role: {}, defaulting to USER", request.getRole());
                user.setRole(UserRole.USER);
            }
        } else {
            user.setRole(UserRole.USER);
        }

        user.setTenant(tenant);

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        return mapToResponse(savedUser);
    }

    public List<UserDTO.UserResponse> getUsersByTenant(String apiKey) {
        Tenant tenant = tenantRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid tenant API key"));

        return userRepository.findByTenantId(tenant.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserDTO.UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return mapToResponse(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        userRepository.delete(user);
        log.info("User deleted with ID: {}", id);
    }

    private UserDTO.UserResponse mapToResponse(User user) {
        UserDTO.UserResponse response = new UserDTO.UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setRole(user.getRole() != null ? user.getRole().toString() : null);
        response.setTenantId(user.getTenant().getId());
        response.setTenantName(user.getTenant().getName());
        return response;
    }
}