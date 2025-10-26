package com.codenine.managementservice.dto.itemLoss;

import java.time.LocalDateTime;

public record ItemLossResponse(
    Long id,
    String reason,
    Integer lostQuantity,
    LocalDateTime createDate,
    LocalDateTime lastUpdate,
    Long itemId,
    String itemName,
    Long recordedById,
    String recordedByName,
    Long lastUserId,
    String lastUserName) {}
