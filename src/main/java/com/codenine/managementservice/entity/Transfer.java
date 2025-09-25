package com.codenine.managementservice.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
