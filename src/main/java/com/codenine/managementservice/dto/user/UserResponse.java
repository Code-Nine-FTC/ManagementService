package com.codenine.managementservice.dto.user;

import java.util.List;

import com.codenine.managementservice.dto.section.SectionDto;

public record UserResponse(
    Long id,
    String username,
    String email,
    Role role,
    boolean isActive,
    List<Long> sectionIds,
    List<SectionDto> sections) {}
