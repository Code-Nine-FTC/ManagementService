package com.codenine.managementservice.dto.order;

public record OrderItemResponse(
    Long id,
    Long itemId,
    String itemName,
    Integer quantity,
    Long supplierId,
    String supplierName) {}
