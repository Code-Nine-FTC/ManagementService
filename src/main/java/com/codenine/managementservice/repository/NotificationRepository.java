package com.codenine.managementservice.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codenine.managementservice.dto.notification.NotificationType;
import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.entity.Notification;
import com.codenine.managementservice.entity.Order;
import com.codenine.managementservice.entity.Transfer;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  boolean existsByTypeAndItemAndExpiresAtAfter(NotificationType type, Item item, Instant now);

  boolean existsByTypeAndOrderAndExpiresAtAfter(NotificationType type, Order order, Instant now);

  boolean existsByTypeAndTransferAndExpiresAtAfter(NotificationType type, Transfer transfer, Instant now);

  List<Notification> findByAcknowledgedFalseAndExpiresAtAfterOrderByCreatedAtDesc(Instant now);
}