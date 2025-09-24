package com.codenine.managementservice.dto.section;

public record SectionFilterCriteria(Long sectionId, Long lastUserId, Integer roleAccess, Boolean isActive) {}