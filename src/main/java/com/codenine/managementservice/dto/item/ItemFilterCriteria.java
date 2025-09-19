package com.codenine.managementservice.dto.item;


public record ItemFilterCriteria(
    Long supplierId,
    Long sectionId,
    Long itemTypeId,
    Long lastUserId,
    Boolean isActive,
    Long itemId
) {}
