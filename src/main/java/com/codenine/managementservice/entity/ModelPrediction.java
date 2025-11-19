package com.codenine.managementservice.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "model_predictions")
public class ModelPrediction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "item_id")
  private Item item;

  @Column(name = "ref_date", nullable = false)
  private LocalDate refDate;

  @Column(name = "horizon_days", nullable = false)
  private Integer horizonDays;

  @Column(name = "y_hat", nullable = false)
  private Double yHat;

  @Column(name = "model_version", nullable = false, length = 50)
  private String modelVersion = "ols_v1";

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();
}
