package com.codenine.managementservice.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "lots")
public class Lot {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "item_id")
  private Item item;

  @Column(nullable = false)
  private String code;

  private LocalDate expireDate;

  @Column(nullable = false)
  private Integer quantityOnHand = 0;

  private LocalDateTime createdAt = LocalDateTime.now();
  private LocalDateTime updatedAt = LocalDateTime.now();
}
