package com.codenine.managementservice.entity;

import java.time.LocalDateTime;

import com.codenine.managementservice.dto.action.ActionType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "audit_logs")
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne private User user;

  @Enumerated(EnumType.STRING)
  private ActionType actionType;

  String entityName;

  private Long entityId;

  private LocalDateTime createdAt = LocalDateTime.now();

  private String details;
}
