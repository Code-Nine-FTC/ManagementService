package com.codenine.managementservice.dto.item;

import java.time.LocalDateTime;

public record ItemRequest(
        String itemCode,
        String name,
        Integer currentStock,
        String measure,
        LocalDateTime expireDate,
        Long itemTypeId,
        Integer minimumStock,
        Integer maximumStock,
        Boolean isActive) {
}
