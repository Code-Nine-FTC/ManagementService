package com.codenine.managementservice.dto.item;

public record ItemFilterCriteria(
        Long sectionId,
        Long itemTypeId,
        Boolean isActive,
        Long itemId) {
}
