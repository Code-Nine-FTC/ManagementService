package com.codenine.managementservice.dto.section;

import java.time.LocalDateTime;

public record SectionResponse(
    Long id,
    String title,
    Integer roleAccess,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime lastUpdate,
    String lastUserName) {}