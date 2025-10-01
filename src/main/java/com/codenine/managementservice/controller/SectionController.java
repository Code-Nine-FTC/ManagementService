package com.codenine.managementservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codenine.managementservice.dto.section.SectionFilterCriteria;
import com.codenine.managementservice.dto.section.SectionRequest;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.service.SectionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/sections")
public class SectionController {

  @Autowired private SectionService sectionService;


  @Operation(description = "Cria uma nova seção/departamento.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados da seção a ser criada")
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> createSection(@RequestBody SectionRequest newSection) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      sectionService.createSection(newSection, lastUser);
      return ResponseEntity.status(201).body("Section created successfully");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(400).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error creating section: " + e.getMessage());
    }
  }


  @Operation(description = "Busca uma seção pelo ID.")
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> getSection(
      @Parameter(description = "ID da seção a ser buscada", example = "1") @PathVariable Long id) {
    try {
      var section = sectionService.getSection(id);
      return ResponseEntity.ok(section);
    } catch (NullPointerException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error retrieving section: " + e.getMessage());
    }
  }

  @Operation(description = "Lista todas as seções, com filtros opcionais.")
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> getAllSections(
      @Parameter(description = "ID da seção", example = "1") @RequestParam(required = false)
          Long sectionId,
      @Parameter(description = "ID do último usuário", example = "2")
          @RequestParam(required = false)
          Long lastUserId,
      @Parameter(description = "Nível de acesso por role", example = "1")
          @RequestParam(required = false)
          Integer roleAccess,
      @Parameter(description = "Status ativo/inativo", example = "true")
          @RequestParam(required = false)
          Boolean isActive) {
    try {
      var sections =
          sectionService.getSectionsByFilter(
              new SectionFilterCriteria(sectionId, lastUserId, roleAccess, isActive));
      return ResponseEntity.ok(sections);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error retrieving sections: " + e.getMessage());
    }
  }


  @Operation(description = "Atualiza uma seção existente.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Novos dados da seção")
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> updateSection(
      @Parameter(description = "ID da seção a ser atualizada", example = "1") @PathVariable Long id,
      @RequestBody SectionRequest updatedSection) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      sectionService.updateSection(id, updatedSection, lastUser);
      return ResponseEntity.ok("Section updated successfully");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(400).body(e.getMessage());
    } catch (NullPointerException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error updating section: " + e.getMessage());
    }
  }

}
