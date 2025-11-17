package com.codenine.managementservice.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codenine.managementservice.dto.action.ActionType;
import com.codenine.managementservice.dto.audit.AuditLogFilterCriteria;
import com.codenine.managementservice.dto.audit.AuditLogResponse;
import com.codenine.managementservice.service.AuditLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/audit-logs")
public class AuditController {

  @Autowired private AuditLogService auditLogService;

  @Operation(
      description =
          "Lista os logs de auditoria com filtros opcionais. Acesso restrito a administradores.")
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> getAuditLogs(
      @Parameter(description = "ID do usuário que realizou a ação", example = "1")
          @RequestParam(required = false)
          Long userId,
      @Parameter(description = "Tipo de ação realizada", example = "SECTION_CREATED")
          @RequestParam(required = false)
          ActionType actionType,
      @Parameter(description = "Data inicial do período", example = "2025-01-01T00:00:00")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime startDate,
      @Parameter(description = "Data final do período", example = "2025-12-31T23:59:59")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime endDate,
      @Parameter(description = "Número da página", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Quantidade de registros por página", example = "20")
          @RequestParam(defaultValue = "20")
          int size) {
    try {
      AuditLogFilterCriteria filterCriteria =
          new AuditLogFilterCriteria(userId, actionType, startDate, endDate);

      Page<AuditLogResponse> auditLogs = auditLogService.getAuditLogs(filterCriteria, page, size);

      return ResponseEntity.ok(auditLogs);
    } catch (Exception e) {
      return ResponseEntity.status(500)
          .body("Error retrieving audit logs: " + e.getMessage());
    }
  }
}
