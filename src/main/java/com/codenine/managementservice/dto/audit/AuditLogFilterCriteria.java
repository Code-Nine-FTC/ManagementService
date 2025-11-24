package com.codenine.managementservice.dto.audit;

import java.time.LocalDateTime;

import com.codenine.managementservice.dto.action.ActionType;

public record AuditLogFilterCriteria(
    Long userId, ActionType actionType, LocalDateTime startDate, LocalDateTime endDate) {}
