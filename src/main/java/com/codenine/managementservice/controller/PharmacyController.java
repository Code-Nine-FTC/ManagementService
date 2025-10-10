package com.codenine.managementservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.codenine.managementservice.service.ItemService;
import com.codenine.managementservice.service.PharmacyService;
import com.codenine.managementservice.service.PharmacyService.ExpiryLists;
import com.codenine.managementservice.service.PharmacyService.ExpirySummary;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/pharmacy")
@Tag(name = "Pharmacy", description = "Endpoints para gerenciamento de medicamentos da farmácia")
public class PharmacyController {

  @Autowired private PharmacyService pharmacyService;

  @Autowired private ItemService itemService;

  @Operation(
      summary = "Resumo de itens vencidos e próximos do vencimento",
      description =
          "Retorna a contagem de todos os itens vencidos e que vão vencer nos próximos dias especificados (padrão: 30 dias = 1 mês), incluindo códigos dos itens")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ASSISTANT')")
  @GetMapping("/items/expiry-summary")
  public ResponseEntity<ExpirySummary> getExpirySummary(
      @Parameter(
              description =
                  "Número de dias para considerar itens próximos do vencimento (padrão: 30 dias = 1 mês)",
              example = "30")
          @RequestParam(defaultValue = "30")
          int days) {
    try {
      ExpirySummary summary = pharmacyService.getAllItemsExpirySummary(days);
      return ResponseEntity.ok(summary);
    } catch (Exception e) {
      return ResponseEntity.status(500).build();
    }
  }

  @Operation(
      summary = "Listas de itens vencidos e próximos do vencimento",
      description =
          "Retorna as listas detalhadas de todos os itens vencidos e que vão vencer nos próximos dias especificados (padrão: 30 dias = 1 mês), incluindo códigos dos itens")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ASSISTANT')")
  @GetMapping("/items/expiry-list")
  public ResponseEntity<ExpiryLists> getExpiryLists(
      @Parameter(
              description =
                  "Número de dias para considerar itens próximos do vencimento (padrão: 30 dias = 1 mês)",
              example = "30")
          @RequestParam(defaultValue = "30")
          int days,
      @Parameter(description = "Número da página para paginação", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Tamanho da página para paginação", example = "20")
          @RequestParam(defaultValue = "20")
          int size) {
    try {
      Pageable pageable = PageRequest.of(page, size);
      ExpiryLists lists = pharmacyService.getAllItemsExpiryLists(days, pageable);
      return ResponseEntity.ok(lists);
    } catch (Exception e) {
      return ResponseEntity.status(500).build();
    }
  }

  @Operation(
      summary = "Excluir item vencido ou a vencer",
      description =
          "Exclui permanentemente um item vencido ou próximo do vencimento do banco de dados. Requer permissão de ADMIN ou MANAGER.")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @DeleteMapping("/items/{id}")
  public ResponseEntity<?> deleteExpiredItem(
      @Parameter(description = "ID do item a ser excluído", example = "1") @PathVariable Long id) {
    try {
      itemService.deleteItem(id);
      return ResponseEntity.ok().body("Item excluído com sucesso");
    } catch (NullPointerException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Erro ao excluir item: " + e.getMessage());
    }
  }
}
