package com.codenine.managementservice.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "transfers")
public class Transfer {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String status;

  private LocalDateTime deliveryDate;

  private LocalDateTime createdAt = LocalDateTime.now();

  private LocalDateTime lastUpdate = LocalDateTime.now();

  @ManyToOne private User lastUser;
}
