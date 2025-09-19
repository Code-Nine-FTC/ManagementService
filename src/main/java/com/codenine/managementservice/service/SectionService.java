package com.codenine.managementservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.entity.Section;
import com.codenine.managementservice.repository.SectionRepository;

@Service
public class SectionService {

  @Autowired private SectionRepository sectionRepository;

  // Implementar CRUD

  private Section getSectionById(Long id) {
    return sectionRepository
        .findById(id)
        .orElseThrow(() -> new NullPointerException("Section not found with id: " + id));
  }
}
