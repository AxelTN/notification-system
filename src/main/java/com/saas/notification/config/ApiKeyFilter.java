package com.saas.notification.config;

import com.saas.notification.repository.TenantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    private final TenantRepository tenantRepository;

    @Value("${api.key.header:X-API-Key}")
    private String apiKeyHeader;

    // List of public endpoints that don't require API key
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/tenants",
            "/api/tenants/",
            "/swagger-ui",
            "/api-docs",
            "/ws",
            "/ws-notifications",
            "/actuator"
    );

    // List of public GET endpoints for notifications
    private static final List<String> PUBLIC_GET_ENDPOINTS = List.of(
            "/api/notifications/user/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Handle CORS preflight OPTIONS request
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;  // Immediately return without further processing
        }

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Check if endpoint is public
        if (isPublicEndpoint(path, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get API key from header
        String apiKey = request.getHeader(apiKeyHeader);

        if (apiKey == null || apiKey.isEmpty()) {
            sendError(response, 401, "API Key is missing");
            return;
        }

        // Validate API key against database
        var tenant = tenantRepository.findByApiKey(apiKey);
        if (tenant.isEmpty()) {
            sendError(response, 401, "Invalid API Key");
            return;
        }

        // Set tenant context
        TenantContext.setTenantApiKey(apiKey);
        TenantContext.setTenantId(tenant.get().getId());
        request.setAttribute("tenantId", tenant.get().getId());
        request.setAttribute("tenant", tenant.get());

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private boolean isPublicEndpoint(String path, String method) {
        // Check exact matches
        for (String publicPath : PUBLIC_ENDPOINTS) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }

        // Check public GET endpoints for notifications
        if (method.equals("GET")) {
            for (String publicGetPath : PUBLIC_GET_ENDPOINTS) {
                if (path.startsWith(publicGetPath)) {
                    return true;
                }
            }
        }

        // POST to /api/tenants is public (creating tenant)
        if (method.equals("POST") && path.equals("/api/tenants")) {
            return true;
        }

        return false;
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(String.format("{\"error\": \"%s\"}", message));
    }
}