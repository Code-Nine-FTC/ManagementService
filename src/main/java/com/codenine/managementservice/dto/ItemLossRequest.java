package com.codenine.managementservice.dto;

public record ItemLossRequest(
    String reason,
    Integer lostQuantity,
    Long itemId,
    Long recordedById
) {
    
}
