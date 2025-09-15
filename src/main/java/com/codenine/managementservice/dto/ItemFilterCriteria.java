package com.codenine.managementservice.dto;


public record ItemFilterCriteria(
    Long supplierId,
    Long sectionId,
    Long itemTypeId,
    Long lastUserId,
    Boolean isActive,
    Long itemId
) {}
