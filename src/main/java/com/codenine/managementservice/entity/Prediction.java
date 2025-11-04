package com.codenine.managementservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "predictions", 
    schema = "ml",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_prediction_item_month", 
            columnNames = {"item_id", "prediction_month"}
        )
    },
    indexes = {
        @Index(name = "idx_ml_predictions_item_id", columnList = "item_id"),
        @Index(name = "idx_ml_predictions_month", columnList = "prediction_month"),
        @Index(name = "idx_ml_predictions_created_at", columnList = "created_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "prediction_month", nullable = false)
    private LocalDate predictionMonth;

    @Column(name = "predicted_quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal predictedQuantity;

    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @Column(name = "model_version", length = 50)
    private String modelVersion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
