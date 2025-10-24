package com.codenine.managementservice.dto.order;

import java.time.LocalDateTime;

public record OrderResponse(
    Long id,
    String orderNumber,
    LocalDateTime withdrawDay,
    String status,
    Long createdById,
    String createdByName,
    Long lastUserId,
    String lastUserName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long sectionId,
    String sectionName) {}
