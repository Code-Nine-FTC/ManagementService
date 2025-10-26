package com.codenine.managementservice.utils.mapper;

import com.codenine.managementservice.dto.section.SectionRequest;
import com.codenine.managementservice.dto.section.SectionResponse;
import com.codenine.managementservice.entity.Section;
import com.codenine.managementservice.entity.SectionType;
import com.codenine.managementservice.entity.User;

public class SectionMapper {
  public static Section toEntity(SectionRequest request, User lastUser) {
    Section section = new Section();
    section.setTitle(request.title());
    section.setRoleAccess(request.roleAccess());
    section.setIsActive(request.isActive() != null ? request.isActive() : true);
    section.setSectionType(
        request.sectionType() != null ? request.sectionType() : SectionType.CONSUMER);
    section.setLastUser(lastUser);
    return section;
  }

  public static void updateEntity(Section section, SectionRequest request, User lastUser) {
    if (request.title() != null) section.setTitle(request.title());
    if (request.roleAccess() != null) section.setRoleAccess(request.roleAccess());
    if (request.isActive() != null) section.setIsActive(request.isActive());
    if (request.sectionType() != null) section.setSectionType(request.sectionType());
    if (request.title() != null || request.roleAccess() != null || request.isActive() != null) {
      section.setLastUpdate(java.time.LocalDateTime.now());
      section.setLastUser(lastUser);
    }
  }

  public static SectionResponse toResponse(Section section) {
    return new SectionResponse(
        section.getId(),
        section.getTitle(),
        section.getRoleAccess(),
        section.getIsActive(),
        section.getSectionType(),
        section.getCreatedAt(),
        section.getLastUpdate(),
        section.getLastUser() != null ? section.getLastUser().getName() : null);
  }
}
