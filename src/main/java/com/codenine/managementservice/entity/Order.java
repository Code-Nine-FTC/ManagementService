package com.codenine.managementservice.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "orders")
public class Order {
  @GeneratedValue @Id private Long id;

  // Número manual do pedido (único e obrigatório)
  @jakarta.persistence.Column(name = "order_number", unique = true, nullable = false, length = 50)
  private String orderNumber;

  private LocalDateTime withdrawDay;

  private String status;

  private LocalDateTime createdAt = LocalDateTime.now();

  private LocalDateTime lastUpdate = LocalDateTime.now();

  private LocalDateTime expireAt;

  @ManyToOne private User createdBy;

  // Supplier linkage removed

  @ManyToOne private User lastUser;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
  private List<OrderItem> orderItems;

  @ManyToOne private Section section;
}
