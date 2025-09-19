package com.codenine.managementservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codenine.managementservice.entity.Section;

public interface SectionRepository extends JpaRepository<Section, Long> {}
