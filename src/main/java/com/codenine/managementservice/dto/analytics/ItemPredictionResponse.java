package com.codenine.managementservice.dto.analytics;

import java.time.LocalDate;

public record ItemPredictionResponse(
    Long itemId,
    String itemName,
    LocalDate refDate,
    int horizonDays,
    double yHat,
    String version) {}
