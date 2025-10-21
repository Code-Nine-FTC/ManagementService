package com.codenine.managementservice.entity;

import java.time.Instant;

import com.codenine.managementservice.dto.notification.NotificationSeverity;
import com.codenine.managementservice.dto.notification.NotificationType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import jakarta.persistence.*;
@Data
@Entity
@Table(name = "notifications")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationSeverity severity;

  @Column(nullable = false, length = 500)
  private String message;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "item_id")
  @JsonIgnoreProperties({"notifications", "orderItems"})
  private Item item;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  @JsonIgnoreProperties({"notifications", "orderItems", "lastUser", "createdBy"})
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "transfer_id")
  @JsonIgnoreProperties({"notifications", "lastUser"})
  private Transfer transfer;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant expiresAt;

  @Column(nullable = false)
  private Boolean acknowledged = false;

}
