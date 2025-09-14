package com.codenine.managementservice.dto;

import java.time.LocalDateTime;

public record ItemResponse(
    Long itemId,
    String name,
    Integer currentStock,
    String measure,
    LocalDateTime expireDate,
    Long supplierId,
    String supplierName,
    Long sectionId,
    String sectionName,
    Long typeItemId,
    String typeItemName,
    Integer minimumStock,
    String qrCode,
    String lastUserName,
    LocalDateTime lastUpdate
) {
}
