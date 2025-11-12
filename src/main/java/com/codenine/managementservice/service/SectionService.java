package com.codenine.managementservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.dto.action.ActionType;
import com.codenine.managementservice.dto.section.SectionFilterCriteria;
import com.codenine.managementservice.dto.section.SectionRequest;
import com.codenine.managementservice.dto.section.SectionResponse;
import com.codenine.managementservice.entity.Section;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.SectionRepository;
import com.codenine.managementservice.utils.mapper.SectionMapper;

@Service
public class SectionService {

  @Autowired private SectionRepository sectionRepository;
  @Autowired private AuditLogService auditLogService;

  public void createSection(SectionRequest newSection, User lastUser) {
    if (newSection.title() == null || newSection.title().trim().isEmpty()) {
      throw new IllegalArgumentException("Section title is required.");
    }
    Section section = SectionMapper.toEntity(newSection, lastUser);
    sectionRepository.save(section);
    auditLogService.logAction(
        lastUser,
        ActionType.SECTION_CREATED,
        section.getId(),
        "Section created with title: " + newSection.title(),
        "Section");
  }

  public void updateSection(Long id, SectionRequest updatedSection, User lastUser) {
    Section section = getSectionById(id);
    if (updatedSection.title() != null && updatedSection.title().trim().isEmpty()) {
      throw new IllegalArgumentException("Section title cannot be empty.");
    }
    SectionMapper.updateEntity(section, updatedSection, lastUser);
    sectionRepository.save(section);
    auditLogService.logAction(
        lastUser,
        ActionType.SECTION_UPDATED,
        section.getId(),
        "Section updated with title: " + updatedSection.title(),
        "Section");
  }

  public SectionResponse getSection(Long id) {
    return sectionRepository.findAllSectionResponses(id, null, null, null).stream()
        .findFirst()
        .orElseThrow(() -> new NullPointerException("Section not found with id: " + id));
  }

  public List<SectionResponse> getSectionsByFilter(SectionFilterCriteria filterCriteria) {
    return sectionRepository.findAllSectionResponses(
        filterCriteria.sectionId(),
        filterCriteria.lastUserId(),
        filterCriteria.isActive(),
        filterCriteria.sectionType());
  }

  private Section getSectionById(Long id) {
    return sectionRepository
        .findById(id)
        .orElseThrow(() -> new NullPointerException("Section not found with id: " + id));
  }
}
