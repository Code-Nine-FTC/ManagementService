package com.codenine.managementservice.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "orders")
public class Order {
  @GeneratedValue
  @Id
  private Long id;

  private LocalDateTime withdrawDay;

  private String status;

  private LocalDateTime createdAt = LocalDateTime.now();

  private LocalDateTime lastUpdate = LocalDateTime.now();

  @ManyToOne
  private User createdBy;

  @OneToMany
  private SupplierCompany supplierCompany;

  @ManyToOne
  private User lastUser;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
  private List<OrderItem> orderItems;

  @ManyToOne
  private Section section;
}
