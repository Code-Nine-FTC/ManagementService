package com.codenine.managementservice.dto;


public record ItemFilterCriteria(
    Integer supplierId,
    Integer sectionId,
    Integer typeItemId,
    Integer lastUserId,
    Boolean isActive,
    Integer itemId
) {}
