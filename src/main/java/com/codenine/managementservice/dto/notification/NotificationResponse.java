package com.codenine.managementservice.dto.notification;

import java.time.Instant;

public record NotificationResponse(
    Long id,
    NotificationType type,
    String message,
    NotificationSeverity severity,
    Long itemId,
    String itemName,
    Long orderId,
    Long transferId,
    Instant createdAt,
    Instant expiresAt,
    Boolean acknowledged
) {}
