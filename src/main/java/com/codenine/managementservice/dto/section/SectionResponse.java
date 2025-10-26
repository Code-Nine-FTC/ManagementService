package com.codenine.managementservice.dto.section;

import java.time.LocalDateTime;

import com.codenine.managementservice.entity.SectionType;

public record SectionResponse(
    Long id,
    String title,
    Integer roleAccess,
    Boolean isActive,
    SectionType sectionType,
    LocalDateTime createdAt,
    LocalDateTime lastUpdate,
    String lastUserName) {}
