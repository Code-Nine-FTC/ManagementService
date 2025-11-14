package com.codenine.managementservice.dto.section;

import com.codenine.managementservice.entity.SectionType;

public record SectionFilterCriteria(
    Long sectionId,
    Long lastUserId,
    Boolean isActive,
    SectionType sectionType) {}
