package com.codenine.managementservice.dto;


import java.time.LocalDateTime;


public record ItemRequest(
    String name, 
    Integer currentStock,
    String measure,
    LocalDateTime expireDate,
    Long supplierId, 
    Long itemTypeId,
    Integer minimumStock,
    Integer maximumStock,
    Boolean isActive
) {
}
