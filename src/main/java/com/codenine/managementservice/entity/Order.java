package com.codenine.managementservice.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "orders")
public class Order {
  @GeneratedValue @Id private Long id;

  private LocalDateTime withdrawDay;

  private String status;

  private LocalDateTime createdAt = LocalDateTime.now();

  private LocalDateTime lastDay = LocalDateTime.now();

  private LocalDateTime lastUpdate = LocalDateTime.now();

  @ManyToOne private User createdBy;

  @ManyToOne private User lastUser;

  @ManyToMany
  @JoinTable(
      name = "order_item",
      joinColumns = @JoinColumn(name = "order_id"),
      inverseJoinColumns = @JoinColumn(name = "item_id"))
  private List<Item> items;

  @ManyToMany
  @JoinTable(
      name = "supplier_order",
      joinColumns = @JoinColumn(name = "order_id"),
      inverseJoinColumns = @JoinColumn(name = "supplier_id"))
  private List<SupplierCompany> suppliers;

  public void updateStatus(String newStatus, User lastUser) {
    this.status = newStatus;
    this.lastUser = lastUser;
    this.lastUpdate = java.time.LocalDateTime.now();
  }
}
