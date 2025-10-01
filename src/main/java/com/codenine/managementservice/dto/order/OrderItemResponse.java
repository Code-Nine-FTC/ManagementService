package com.codenine.managementservice.dto.order;

public record OrderItemResponse(
        Long id,
        Long ordemId,
        Long itemId,
        String itemName,
        Integer quantity) {
}
