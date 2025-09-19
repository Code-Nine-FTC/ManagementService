package com.codenine.managementservice.dto.itemLoss;

public record ItemLossRequest(
    String reason, Integer lostQuantity, Long itemId, Long recordedById) {}
