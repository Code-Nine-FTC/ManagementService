package com.codenine.managementservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.dto.action.ActionType;
import com.codenine.managementservice.dto.audit.AuditLogFilterCriteria;
import com.codenine.managementservice.dto.audit.AuditLogResponse;
import com.codenine.managementservice.entity.AuditLog;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.AuditRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditLogService {

  private final AuditRepository auditRepository;

  public void logAction(
      User user, ActionType actionType, Long entityId, String details, String entityName) {
    AuditLog auditLog = new AuditLog();
    auditLog.setUser(user);
    auditLog.setActionType(actionType);
    auditLog.setEntityId(entityId);
    auditLog.setDetails(details);
    auditLog.setEntityName(entityName);
    auditRepository.save(auditLog);
  }

  public Page<AuditLogResponse> getAuditLogs(
      AuditLogFilterCriteria filterCriteria, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);

    Page<AuditLog> auditLogs =
        auditRepository.findAllWithFilters(
            filterCriteria.userId(),
            filterCriteria.actionType(),
            filterCriteria.startDate(),
            filterCriteria.endDate(),
            pageable);

    return auditLogs.map(this::mapToResponse);
  }

  private AuditLogResponse mapToResponse(AuditLog auditLog) {
    return new AuditLogResponse(
        auditLog.getId(),
        auditLog.getUser() != null ? auditLog.getUser().getName() : "Unknown",
        auditLog.getUser() != null ? auditLog.getUser().getEmail() : "Unknown",
        auditLog.getActionType(),
        auditLog.getEntityName(),
        auditLog.getEntityId(),
        auditLog.getDetails(),
        auditLog.getCreatedAt());
  }
}
