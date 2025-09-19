package com.codenine.managementservice.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "item_losses")
public class ItemLoss {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String reason;

  private Integer lostQuantity;

  @Column(nullable = false)
  private LocalDateTime createDate = LocalDateTime.now();

  @Column(nullable = false)
  private LocalDateTime lastUpdate = LocalDateTime.now();

  @ManyToOne private User recordedBy;

  @ManyToOne private Item item;

  @ManyToOne private User lastUser;
}
