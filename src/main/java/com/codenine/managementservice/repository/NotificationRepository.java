package com.codenine.managementservice.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codenine.managementservice.dto.notification.NotificationType;
import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.entity.Notification;
import com.codenine.managementservice.entity.Order;
import com.codenine.managementservice.entity.Transfer;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  @Query(
      "select (count(n) > 0) from Notification n where n.type = :type and n.item = :item and n.expiresAt > :now")
  boolean existsByTypeAndItemAndExpiresAtAfter(
      @Param("type") NotificationType type, @Param("item") Item item, @Param("now") Instant now);

  @Query(
      "select (count(n) > 0) from Notification n where n.type = :type and n.order = :order and n.expiresAt > :now")
  boolean existsByTypeAndOrderAndExpiresAtAfter(
      @Param("type") NotificationType type, @Param("order") Order order, @Param("now") Instant now);

  @Query(
      "select (count(n) > 0) from Notification n where n.type = :type and n.transfer = :transfer and n.expiresAt > :now")
  boolean existsByTypeAndTransferAndExpiresAtAfter(
      @Param("type") NotificationType type,
      @Param("transfer") Transfer transfer,
      @Param("now") Instant now);

  List<Notification> findByAcknowledgedFalseAndExpiresAtAfterOrderByCreatedAtDesc(Instant now);

  @Query(
      "select (count(n) > 0) from Notification n where n.type = :type and n.order = :order and n.createdAt > :timeLimit")
  boolean existsByTypeAndOrderAndCreatedAtAfter(
      @Param("type") NotificationType type,
      @Param("order") Order order,
      @Param("timeLimit") Instant timeLimit);

  @Query(
      "select (count(n) > 0) from Notification n where n.type = :type and n.transfer = :transfer and n.createdAt > :timeLimit")
  boolean existsByTypeAndTransferAndCreatedAtAfter(
      @Param("type") NotificationType type,
      @Param("transfer") Transfer transfer,
      @Param("timeLimit") Instant timeLimit);
}
