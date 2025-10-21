package com.codenine.managementservice.dto.notification;

public enum NotificationType {
  LOW_STOCK,
  CRITICAL_STOCK,
  OUT_OF_STOCK,
  ORDER_CREATED,
  ORDER_STATUS_CHANGED,
  TRANSFER_CREATED,
  TRANSFER_STATUS_CHANGED,
  TRANSFER_DEADLINE_NEAR,
  TRANSFER_OVERDUE
}