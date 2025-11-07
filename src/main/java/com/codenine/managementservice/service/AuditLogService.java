package com.codenine.managementservice.service;

import org.springframework.stereotype.Service;

import com.codenine.managementservice.dto.action.ActionType;
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
}
