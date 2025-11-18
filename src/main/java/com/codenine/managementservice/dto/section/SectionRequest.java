package com.codenine.managementservice.dto.section;

import com.codenine.managementservice.entity.SectionType;

public record SectionRequest(
    String title, Boolean isActive, SectionType sectionType) {}
