package com.codenine.managementservice.dto.item;

public record ItemFilterCriteria(
        String itemCode,
        Long sectionId,
        Long itemTypeId,
        Boolean isActive,
        Long itemId) {
}
