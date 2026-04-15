package com.saas.notification.config;

public class TenantContext {
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();
    private static final ThreadLocal<Long> currentTenantId = new ThreadLocal<>();

    public static void setTenantApiKey(String apiKey) {
        currentTenant.set(apiKey);
    }

    public static String getTenantApiKey() {
        return currentTenant.get();
    }

    public static void setTenantId(Long tenantId) {
        currentTenantId.set(tenantId);
    }

    public static Long getTenantId() {
        return currentTenantId.get();
    }

    public static void clear() {
        currentTenant.remove();
        currentTenantId.remove();
    }
}