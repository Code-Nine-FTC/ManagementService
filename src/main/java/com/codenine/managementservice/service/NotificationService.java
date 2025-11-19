package com.codenine.managementservice.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codenine.managementservice.dto.notification.NotificationResponse;
import com.codenine.managementservice.dto.notification.NotificationSeverity;
import com.codenine.managementservice.dto.notification.NotificationType;
import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.entity.ModelPrediction;
import com.codenine.managementservice.entity.Notification;
import com.codenine.managementservice.entity.Order;
import com.codenine.managementservice.entity.Transfer;
import com.codenine.managementservice.repository.ModelPredictionRepository;
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

    if (type == NotificationType.OUT_OF_STOCK
        || type == NotificationType.LOW_STOCK
        || type == NotificationType.CRITICAL_STOCK
        || type == NotificationType.REORDER_RECOMMENDATION
        || type == NotificationType.MIN_STOCK_TOO_LOW) {
      boolean exists = notificationRepository.existsByTypeAndItemAndExpiresAtAfter(type, item, now);
      if (exists) return;
    } else if (type == NotificationType.ORDER_CREATED) {
      boolean exists =
          notificationRepository.existsByTypeAndOrderAndExpiresAtAfter(type, order, now);
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

    if (type == NotificationType.TRANSFER_DEADLINE_NEAR
        || type == NotificationType.TRANSFER_OVERDUE) {
      boolean exists =
          notificationRepository.existsByTypeAndTransferAndExpiresAtAfter(type, transfer, now);
      if (exists) return;
    } else if (type == NotificationType.TRANSFER_CREATED) {
      boolean exists =
          notificationRepository.existsByTypeAndTransferAndExpiresAtAfter(type, transfer, now);
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

    return notifications.stream().map(this::toResponse).collect(Collectors.toList());
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
        n.getAcknowledged());
  }

  @Transactional
  public void acknowledgeNotification(Long notificationId) {
    Notification notification =
        notificationRepository
            .findById(notificationId)
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

// Mantido como top-level (package-private) para compartilhar arquivo com NotificationService.
// Spring detecta normalmente via @Service.
@Service
class PredictionNotificationService {

  private static final Logger log = LoggerFactory.getLogger(PredictionNotificationService.class);

  @Value("${ai.lead-time-days:7}")
  private int leadTimeDays;

  @Value("${ai.restock-buffer-days:7}")
  private int restockBufferDays;

  @Value("${ai.notification-ttl-seconds:604800}") // 7 dias
  private long notificationTtlSeconds;

  @Value("${ai.default-horizon-days:14}")
  private int defaultHorizonDays;

  @Autowired private ModelPredictionRepository predictionRepository;
  @Autowired private NotificationService notificationService;

  /** Gera notificações com base nas últimas previsões. Agenda diária às 02:20. */
  @Scheduled(cron = "0 20 2 * * *")
  @Transactional
  public void notifyFromLatestPredictions() {
    try {
      int horizon = defaultHorizonDays > 0 ? defaultHorizonDays : 14;
      LocalDate ref = predictionRepository.findMaxRefDateByHorizon(horizon);
      if (ref == null) {
        log.info("PredictionNotificationService: sem previsões para horizon={}.", horizon);
        return;
      }

      List<ModelPrediction> preds = predictionRepository.findByRefDateAndHorizonDays(ref, horizon);
      int count = 0;

      for (ModelPrediction p : preds) {
        Item item = p.getItem();
        if (item == null || (item.getIsActive() != null && !item.getIsActive())) continue;

        int currentStock = item.getCurrentStock() != null ? item.getCurrentStock() : 0;
        Integer minStock = item.getMinimumStock();
        double sumPred = p.getYHat() != null ? p.getYHat() : 0d;
        double avgDaily = horizon > 0 ? sumPred / horizon : 0d;

        // Alerta de mínimo baixo (com base no lead time)
        if (minStock != null && minStock > 0 && avgDaily > 0d) {
          int suggestedMin = (int) Math.ceil(avgDaily * leadTimeDays);
          if (minStock < suggestedMin) {
            String msg =
                String.format(
                    "Estoque mínimo configurado (%d) pode estar baixo para '%s'. "
                        + "Consumo previsto médio: %.2f/dia; mínimo sugerido: %d.",
                    minStock, item.getName(), avgDaily, suggestedMin);
            notificationService.createNotification(
                NotificationType.MIN_STOCK_TOO_LOW,
                msg,
                NotificationSeverity.WARNING,
                item,
                null,
                notificationTtlSeconds);
          }
        }

        // Recomendação de compra
        int targetDays = leadTimeDays + restockBufferDays;
        int targetStock = (int) Math.ceil(Math.max(0d, avgDaily) * targetDays);
        int recommendedQty = Math.max(0, targetStock - currentStock);
        boolean belowMin = (minStock != null && currentStock < minStock);

        if (recommendedQty > 0 || belowMin) {
          String msg =
              String.format(
                  "[IA] Recomendação: '%s'. Previsto %,.0f un. próximos %d dias (%.2f/dia). "
                      + "Estoque atual: %d. Sugerido pedir: %d (cobertura alvo: %d dias).",
                  item.getName(),
                  sumPred,
                  horizon,
                  avgDaily,
                  currentStock,
                  recommendedQty,
                  targetDays);
          notificationService.createNotification(
              NotificationType.REORDER_RECOMMENDATION,
              msg,
              NotificationSeverity.INFO,
              item,
              null,
              notificationTtlSeconds);
          count++;
        }
      }

      log.info(
          "PredictionNotificationService: notificações processadas para ref={} ({} recomendações)",
          ref,
          count);
    } catch (Exception e) {
      log.error("PredictionNotificationService: erro ao gerar notificações", e);
    }
  }
}
