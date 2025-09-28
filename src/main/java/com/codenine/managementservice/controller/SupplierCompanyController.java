package com.codenine.managementservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.codenine.managementservice.dto.supplier.SupplierCompanyRequest;
import com.codenine.managementservice.dto.supplier.SupplierCompanyResponse;
import com.codenine.managementservice.service.SupplierCompanyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/suppliers")
public class SupplierCompanyController {
  @Autowired private SupplierCompanyService supplierCompanyService;

  /**
   * Lista todas as empresas fornecedoras com filtros opcionais.
   *
   * @param supplierId ID específico do fornecedor (opcional)
   * @param isActive Status ativo/inativo (opcional)
   * @param lastUserId ID do usuário que fez a última modificação (opcional)
   * @return Lista de fornecedores.
   */
  @Operation(description = "Lista todas as empresas fornecedoras com filtros opcionais.")
  @GetMapping
  public ResponseEntity<List<SupplierCompanyResponse>> getAllSuppliers(
      @Parameter(description = "ID específico do fornecedor", example = "1")
          @RequestParam(required = false)
          Long supplierId,
      @Parameter(description = "Status ativo/inativo", example = "true")
          @RequestParam(required = false)
          Boolean isActive,
      @Parameter(description = "ID do usuário que fez a última modificação", example = "1")
          @RequestParam(required = false)
          Long lastUserId) {
    return ResponseEntity.ok(
        supplierCompanyService.getSupplierCompaniesWithFilters(supplierId, isActive, lastUserId));
  }

  /**
   * Busca uma empresa fornecedora pelo ID.
   *
   * @param id ID do fornecedor.
   * @return Dados do fornecedor ou 404 se não encontrado.
   */
  @Operation(description = "Busca uma empresa fornecedora pelo ID.")
  @GetMapping("/{id}")
  public ResponseEntity<SupplierCompanyResponse> getSupplier(
      @Parameter(description = "ID do fornecedor a ser buscado", example = "1") @PathVariable
          Long id) {
    try {
      SupplierCompanyResponse response = supplierCompanyService.getSupplierCompany(id);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Cria uma nova empresa fornecedora.
   *
   * @param supplierCompanyRequest Dados do fornecedor a ser criado.
   * @return Dados do fornecedor criado.
   */
  @Operation(description = "Cria uma nova empresa fornecedora.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "Dados do fornecedor a ser criado")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<SupplierCompanyResponse> createSupplier(
      @org.springframework.web.bind.annotation.RequestBody
          SupplierCompanyRequest supplierCompanyRequest) {
    try {
      SupplierCompanyResponse created =
          supplierCompanyService.createSupplierCompany(supplierCompanyRequest);
      return ResponseEntity.ok(created);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Atualiza uma empresa fornecedora existente.
   *
   * @param id ID do fornecedor.
   * @param supplierCompanyRequest Novos dados do fornecedor.
   * @return Dados do fornecedor atualizado.
   */
  @Operation(description = "Atualiza uma empresa fornecedora existente.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Novos dados do fornecedor")
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public ResponseEntity<SupplierCompanyResponse> updateSupplier(
      @Parameter(description = "ID do fornecedor a ser atualizado", example = "1") @PathVariable
          Long id,
      @org.springframework.web.bind.annotation.RequestBody
          SupplierCompanyRequest supplierCompanyRequest) {
    try {
      SupplierCompanyResponse updated =
          supplierCompanyService.updateSupplierCompany(id, supplierCompanyRequest);
      return ResponseEntity.ok(updated);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Remove uma empresa fornecedora pelo ID.
   *
   * @param id ID do fornecedor.
   * @return Sem conteúdo em caso de sucesso.
   */
  @Operation(description = "Remove uma empresa fornecedora pelo ID.")
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteSupplier(
      @Parameter(description = "ID do fornecedor a ser removido", example = "1") @PathVariable
          Long id) {
    try {
      supplierCompanyService.deleteSupplierCompany(id);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      return ResponseEntity.notFound().build();
    }
  }
}
