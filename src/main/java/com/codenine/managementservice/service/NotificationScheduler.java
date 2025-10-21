package com.codenine.managementservice.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.dto.notification.NotificationSeverity;
import com.codenine.managementservice.dto.notification.NotificationType;
import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.entity.Order;
import com.codenine.managementservice.entity.Transfer;
import com.codenine.managementservice.repository.ItemRepository;
import com.codenine.managementservice.repository.OrderRepository;
import com.codenine.managementservice.repository.TransferRepository;

@Service
public class NotificationScheduler {

  @Autowired private ItemRepository itemRepository;

  @Autowired private OrderRepository orderRepository;

  @Autowired private TransferRepository transferRepository;

  @Autowired private NotificationService notificationService;

  @Scheduled(fixedRate = 60 * 1000) // Roda a cada 1 min
  public void checkAndCreateNotifications() {
    checkStockLevels();
    checkTransferDeadlines();
    notificationService.cleanupExpired();
  }

  private void checkStockLevels() {
    List<Item> items = itemRepository.findAll();
    for (Item item : items) {
      if (item.getMinimumStock() == null) continue;
      
      int min = item.getMinimumStock();
      int current = item.getCurrentStock() == null ? 0 : item.getCurrentStock();

      if (current == 0) {
        notificationService.createNotification(
            NotificationType.OUT_OF_STOCK,
            "Estoque zerado: " + item.getName(),
            NotificationSeverity.CRITICAL,
            item,
            null,
            7776000L); // formato de 90 dias de validade da not
      }
      else if (current == min) {
        notificationService.createNotification(
            NotificationType.LOW_STOCK,
            "Estoque no mínimo: " + item.getName() + " (atual: " + current + ")",
            NotificationSeverity.WARNING,
            item,
            null,
            7776000L);
      }
    }
  }

  private void checkTransferDeadlines() {
    List<Transfer> transfers = transferRepository.findAll();
    LocalDateTime now = LocalDateTime.now();

    for (Transfer transfer : transfers) {
      if (transfer.getDeliveryDate() == null) continue;
      if ("COMPLETED".equals(transfer.getStatus()) || "CANCELLED".equals(transfer.getStatus())) continue;

      LocalDateTime deliveryDate = transfer.getDeliveryDate();
      long daysUntilDelivery = ChronoUnit.DAYS.between(now, deliveryDate);

      if (deliveryDate.isBefore(now)) {
        notificationService.createNotificationForTransfer(
            NotificationType.TRANSFER_OVERDUE,
            "Transferência #" + transfer.getId() + " vencida! Prazo era " + deliveryDate,
            NotificationSeverity.CRITICAL,
            transfer,
            7776000L);
      }
      else if (daysUntilDelivery <= 7 && daysUntilDelivery >= 0) {
        notificationService.createNotificationForTransfer(
            NotificationType.TRANSFER_DEADLINE_NEAR,
            "Transferência #" + transfer.getId() + " próxima do prazo! Faltam " + daysUntilDelivery + " dias",
            NotificationSeverity.WARNING,
            transfer,
            7776000L);
      }
    }
  }
}