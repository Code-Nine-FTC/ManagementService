package com.codenine.managementservice.dto;


import java.time.LocalDateTime;


public record ItemRequest(
    String name, 
    Integer currentStock,
    String measure,
    LocalDateTime expireDate,
    Long supplierId, 
    Long sectionId, 
    Long typeItemId, 
    Integer minimumStock,
    String qrCode,
    Long lastUserId
) {
}
