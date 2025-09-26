package com.codenine.managementservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

  public void createSection(SectionRequest newSection, User lastUser) {
    if (newSection.title() == null || newSection.title().trim().isEmpty()) {
      throw new IllegalArgumentException("Section title is required.");
    }
    Section section = SectionMapper.toEntity(newSection, lastUser);
    sectionRepository.save(section);
  }

  public void updateSection(Long id, SectionRequest updatedSection, User lastUser) {
    Section section = getSectionById(id);
    if (updatedSection.title() != null && updatedSection.title().trim().isEmpty()) {
      throw new IllegalArgumentException("Section title cannot be empty.");
    }
    SectionMapper.updateEntity(section, updatedSection, lastUser);
    sectionRepository.save(section);
  }

  public SectionResponse getSection(Long id) {
    return sectionRepository.findAllSectionResponses(id, null, null, null).stream()
        .findFirst()
        .orElseThrow(() -> new NullPointerException("Section not found with id: " + id));
  }

  public List<SectionResponse> getSectionsByFilter(SectionFilterCriteria filterCriteria) {
    return sectionRepository.findAllSectionResponses(
        filterCriteria.sectionId(), filterCriteria.lastUserId(),
        filterCriteria.roleAccess(), filterCriteria.isActive());
  }

  public void disableSection(Long id, User lastUser) {
    Section section = getSectionById(id);
    section.setIsActive(false);
    section.setLastUser(lastUser);
    section.setLastUpdate(java.time.LocalDateTime.now());
    sectionRepository.save(section);
  }

  public void deleteSection(Long id, User lastUser) {
    Section section = getSectionById(id);
    if (section.getItemTypes() != null && !section.getItemTypes().isEmpty()) {
      throw new IllegalStateException("Cannot delete section with associated item types.");
    }
    if (section.getUsers() != null && !section.getUsers().isEmpty()) {
      throw new IllegalStateException("Cannot delete section with associated users.");
    }
    sectionRepository.delete(section);
  }

  private Section getSectionById(Long id) {
    return sectionRepository
        .findById(id)
        .orElseThrow(() -> new NullPointerException("Section not found with id: " + id));
  }
}
