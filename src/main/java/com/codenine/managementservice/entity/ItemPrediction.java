package com.codenine.managementservice.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ItemPrediction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long itemId;
  private Integer month;
  private String forecastType;
  private Double value;
  private LocalDateTime createdAt = LocalDateTime.now();
}
