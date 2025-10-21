package com.codenine.managementservice.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codenine.managementservice.dto.notification.NotificationResponse;
import com.codenine.managementservice.dto.notification.NotificationSeverity;
import com.codenine.managementservice.dto.notification.NotificationType;
import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.entity.Notification;
import com.codenine.managementservice.entity.Order;
import com.codenine.managementservice.entity.Transfer;
import com.codenine.managementservice.repository.NotificationRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class NotificationService {

  @Autowired private NotificationRepository notificationRepository;

  @Transactional
  public void createNotification(
      NotificationType type,
      String message,
      NotificationSeverity severity,
      Item item,
      Order order,
      Long expiresInSeconds) {
    Instant now = Instant.now();
    Instant expiresAt = now.plus(expiresInSeconds, ChronoUnit.SECONDS);

    if (type == NotificationType.OUT_OF_STOCK || 
        type == NotificationType.LOW_STOCK || 
        type == NotificationType.CRITICAL_STOCK) {
      boolean exists = notificationRepository.existsByTypeAndItemAndExpiresAtAfter(type, item, now);
      if (exists) return;
    } else if (type == NotificationType.ORDER_CREATED) {
      boolean exists = notificationRepository.existsByTypeAndOrderAndExpiresAtAfter(type, order, now);
      if (exists) return;
    }

    Notification n = new Notification();
    n.setType(type);
    n.setMessage(message);
    n.setSeverity(severity);
    n.setItem(item);
    n.setOrder(order);
    n.setCreatedAt(now);
    n.setExpiresAt(expiresAt);
    n.setAcknowledged(false);

    notificationRepository.save(n);
  }

  @Transactional
  public void createNotificationForTransfer(
      NotificationType type,
      String message,
      NotificationSeverity severity,
      Transfer transfer,
      Long expiresInSeconds) {
    Instant now = Instant.now();
    Instant expiresAt = now.plus(expiresInSeconds, ChronoUnit.SECONDS);

    if (type == NotificationType.TRANSFER_DEADLINE_NEAR || 
        type == NotificationType.TRANSFER_OVERDUE) {
      boolean exists = notificationRepository.existsByTypeAndTransferAndExpiresAtAfter(type, transfer, now);
      if (exists) return;
    } else if (type == NotificationType.TRANSFER_CREATED) {
      boolean exists = notificationRepository.existsByTypeAndTransferAndExpiresAtAfter(type, transfer, now);
      if (exists) return;
    }

    Notification n = new Notification();
    n.setType(type);
    n.setMessage(message);
    n.setSeverity(severity);
    n.setTransfer(transfer);
    n.setCreatedAt(now);
    n.setExpiresAt(expiresAt);
    n.setAcknowledged(false);

    notificationRepository.save(n);
  }

  @Transactional
  public List<NotificationResponse> getUnacknowledgedNotifications() {
    Instant now = Instant.now();
    List<Notification> notifications = 
        notificationRepository.findByAcknowledgedFalseAndExpiresAtAfterOrderByCreatedAtDesc(now);
    
    return notifications.stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  private NotificationResponse toResponse(Notification n) {
    return new NotificationResponse(
        n.getId(),
        n.getType(),
        n.getMessage(),
        n.getSeverity(),
        n.getItem() != null ? n.getItem().getId() : null,
        n.getItem() != null ? n.getItem().getName() : null,
        n.getOrder() != null ? n.getOrder().getId() : null,
        n.getTransfer() != null ? n.getTransfer().getId() : null,
        n.getCreatedAt(),
        n.getExpiresAt(),
        n.getAcknowledged()
    );
  }

  @Transactional
  public void acknowledgeNotification(Long notificationId) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new EntityNotFoundException("Notificação não encontrada"));
    notification.setAcknowledged(true);
    notificationRepository.save(notification);
  }

  @Transactional
  public void acknowledgeAllNotifications() {
    Instant now = Instant.now();
    List<Notification> notifications = 
        notificationRepository.findByAcknowledgedFalseAndExpiresAtAfterOrderByCreatedAtDesc(now);
    notifications.forEach(n -> n.setAcknowledged(true));
    notificationRepository.saveAll(notifications);
  }

  @Transactional
  public void cleanupExpired() {
    Instant now = Instant.now();
    notificationRepository.findAll().stream()
        .filter(n -> n.getExpiresAt().isBefore(now))
        .forEach(notificationRepository::delete);
  }
}
