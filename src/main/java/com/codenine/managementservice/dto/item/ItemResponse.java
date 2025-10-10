package com.codenine.managementservice.dto.item;

import java.time.LocalDateTime;

public record ItemResponse(
    Long itemId,
    String name,
    Integer currentStock,
    String measure,
    LocalDateTime expireDate,
    Long sectionId,
    String sectionName,
    Long itemTypeId,
    String itemTypeName,
    Integer minimumStock,
    String qrCode,
    String itemCode,
    String lastUserName,
    LocalDateTime lastUpdate) {}
