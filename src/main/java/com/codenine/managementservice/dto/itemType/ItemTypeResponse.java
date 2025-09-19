package com.codenine.managementservice.dto.itemType;

public record ItemTypeResponse(
    Long id,
    String name,
    Long sectionId,
    String sectionName,
    Long lastUserId,
    String lastUserName,
    String lastUpdate
) {
    
}
