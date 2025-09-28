package com.codenine.managementservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "order_item")
public class OrderItem {
  @Id
  @GeneratedValue
  private Long id;

  @ManyToOne(cascade = CascadeType.ALL)
  private Order order;

  @ManyToOne
  @JoinColumn(name = "item_id")
  private Item item;

  private Integer quantity;

  @ManyToOne
  private User lastUser;
}
