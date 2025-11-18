package com.codenine.managementservice.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "item_monthly_snapshot")
public class ItemMonthlySnapshot {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne
  @JoinColumn(name = "item_id")
  private Item item;

  Integer yearMonth;
  Float stockQuantity = 0f;
  Integer ordersPlaced = 0;
  Float averageConsumed = 0f;
  LocalDateTime createdAt = LocalDateTime.now();
}
