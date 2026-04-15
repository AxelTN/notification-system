package com.saas.notification.repository;

import com.saas.notification.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);
    List<Notification> findByTenantId(Long tenantId);
    List<Notification> findByUserIdAndStatus(Long userId, String status);
}