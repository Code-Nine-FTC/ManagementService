package com.codenine.managementservice.dto.prediction;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictionApiResponseDTO {
    
    @JsonProperty("item_id")
    private Long itemId;
    
    @JsonProperty("predicted_quantity")
    private BigDecimal predictedQuantity;
    
    @JsonProperty("current_stock")
    private BigDecimal currentStock;
    
    @JsonProperty("minimum_stock")
    private BigDecimal minimumStock;
    
    @JsonProperty("maximum_stock")
    private BigDecimal maximumStock;
    
    @JsonProperty("prediction_month")
    private Integer predictionMonth;
    
    @JsonProperty("prediction_year")
    private Integer predictionYear;
    
    @JsonProperty("needs_restock")
    private Boolean needsRestock;
    
    @JsonProperty("restock_quantity")
    private BigDecimal restockQuantity;
    
    @JsonProperty("confidence_score")
    private BigDecimal confidenceScore;
    
    @JsonProperty("model_used")
    private String modelUsed;
    
    @JsonProperty("timestamp")
    private String timestamp;
}
