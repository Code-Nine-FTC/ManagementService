package com.codenine.managementservice.controller;

import java.util.List;

import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.codenine.managementservice.dto.supplier.SupplierCompanyFilterCriteria;
import com.codenine.managementservice.dto.supplier.SupplierCompanyRequest;
import com.codenine.managementservice.dto.supplier.SupplierCompanyResponse;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.service.SupplierCompanyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/suppliers")
public class SupplierCompanyController {

  @Autowired private SupplierCompanyService supplierCompanyService;

  @Operation(description = "Lista todas as empresas fornecedoras com filtros opcionais.")
  @GetMapping
  public ResponseEntity<?> getAllSuppliers(
      @Parameter(description = "ID específico do fornecedor", example = "1")
          @RequestParam(required = false)
          Long supplierId,
      @Parameter(description = "Status ativo/inativo", example = "true")
          @RequestParam(required = false)
          Boolean isActive) {
    try {
      List<SupplierCompanyResponse> responses =
          supplierCompanyService.getAllSupplierCompanies(
              new SupplierCompanyFilterCriteria(supplierId, isActive));
      return ResponseEntity.ok(responses);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  @Operation(description = "Busca uma empresa fornecedora pelo ID.")
  @GetMapping("/{id}")
  public ResponseEntity<?> getSupplier(
      @Parameter(description = "ID do fornecedor a ser buscado", example = "1") @PathVariable
          Long id) {
    try {
      SupplierCompanyResponse response = supplierCompanyService.getSupplierCompany(id);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.notFound().build();
    }
  }

  @Operation(description = "Cria uma nova empresa fornecedora.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "Dados do fornecedor a ser criado")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<?> createSupplier(
      @org.springframework.web.bind.annotation.RequestBody
          SupplierCompanyRequest supplierCompanyRequest) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      supplierCompanyService.createSupplierCompany(supplierCompanyRequest, lastUser);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @Operation(description = "Atualiza uma empresa fornecedora existente.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Novos dados do fornecedor")
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public ResponseEntity<?> updateSupplier(
      @Parameter(description = "ID do fornecedor a ser atualizado", example = "1") @PathVariable
          Long id,
      @org.springframework.web.bind.annotation.RequestBody
          SupplierCompanyRequest supplierCompanyRequest,
      Authorization authorization) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      supplierCompanyService.updateSupplierCompany(id, supplierCompanyRequest, lastUser);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @Operation(description = "Remove uma empresa fornecedora pelo ID.")
  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/switch/{id}")
  public ResponseEntity<Void> deleteSupplier(
      @Parameter(description = "ID do fornecedor a ser desativado", example = "1") @PathVariable
          Long id,
      Authorization authorization) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      supplierCompanyService.switchSupplierCompanyActive(id, lastUser);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      return ResponseEntity.notFound().build();
    }
  }
}
