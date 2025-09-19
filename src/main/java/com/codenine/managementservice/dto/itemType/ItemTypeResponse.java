package com.codenine.managementservice.dto.itemType;

import java.time.LocalDateTime;

public record ItemTypeResponse(
    Long id,
    String name,
    Long sectionId,
    String sectionName,
    Long lastUserId,
    String lastUserName,
    LocalDateTime lastUpdate) {}
