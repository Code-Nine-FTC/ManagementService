package com.codenine.managementservice.dto.audit;

import java.time.LocalDateTime;

import com.codenine.managementservice.dto.action.ActionType;

public record AuditLogResponse(
    Long id,
    String userName,
    String userEmail,
    ActionType actionType,
    String entityName,
    Long entityId,
    String details,
    LocalDateTime createdAt) {}
