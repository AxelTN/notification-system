package com.saas.notification.controllers;

import com.saas.notification.dto.UserDTO;
import com.saas.notification.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for managing users")
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<UserDTO.UserResponse> createUser(
            @Valid @RequestBody UserDTO.CreateUserRequest request,
            @RequestHeader(value = "X-API-Key", required = true) String apiKey) {

        log.info("API Key from header: {}", apiKey);

        // Set the API key in the request object
        request.setTenantApiKey(apiKey);

        UserDTO.UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/tenant")
    @Operation(summary = "Get all users for current tenant")
    public ResponseEntity<List<UserDTO.UserResponse>> getUsersByTenant(
            @RequestHeader(value = "X-API-Key", required = true) String apiKey) {
        return ResponseEntity.ok(userService.getUsersByTenant(apiKey));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserDTO.UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}