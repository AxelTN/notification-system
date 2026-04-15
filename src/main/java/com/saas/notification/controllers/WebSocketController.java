package com.saas.notification.controllers;

import com.saas.notification.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/send")
    @SendTo("/topic/public")
    public NotificationDTO.NotificationResponse sendNotification(@Payload NotificationDTO.NotificationResponse notification) {
        log.info("Broadcasting notification via WebSocket: {}", notification.getTitle());
        return notification;
    }

    public void sendToUser(Long userId, NotificationDTO.NotificationResponse notification) {
        log.info("Sending WebSocket notification to user: {}", userId);
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                notification
        );
    }

    public void sendToTenant(String tenantId, NotificationDTO.NotificationResponse notification) {
        log.info("Sending WebSocket notification to tenant: {}", tenantId);
        messagingTemplate.convertAndSend("/topic/tenant/" + tenantId, notification);
    }
}