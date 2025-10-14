package com.codenine.managementservice.dto.itemLoss;

import java.time.LocalDateTime;

public record ItemLossFilterCriteria(
    Long itemId,
    Long recordedById,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Long itemLossId) {}
