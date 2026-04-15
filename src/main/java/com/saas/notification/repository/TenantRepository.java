package com.saas.notification.repository;

import com.saas.notification.domain.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByApiKey(String apiKey);
    Optional<Tenant> findByName(String name);
    boolean existsByName(String name);
}