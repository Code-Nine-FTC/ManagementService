package com.codenine.managementservice.dto.prediction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponseDTO {
    
    private Long id;
    private Long itemId;
    private String itemName;
    private LocalDate predictionMonth;
    private BigDecimal predictedQuantity;
    private BigDecimal confidenceScore;
    private String modelVersion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
