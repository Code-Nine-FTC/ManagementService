package com.codenine.managementservice.controller;

import java.time.LocalDateTime;

import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.codenine.managementservice.dto.item.ArchiveItem;
import com.codenine.managementservice.dto.item.ItemFilterCriteria;
import com.codenine.managementservice.dto.item.ItemRequest;
import com.codenine.managementservice.dto.itemLoss.ItemLossFilterCriteria;
import com.codenine.managementservice.dto.itemLoss.ItemLossRequest;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.service.ItemLossService;
import com.codenine.managementservice.service.ItemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/items")
public class ItemController {

  @Autowired private ItemService itemService;

  @Autowired private ItemLossService itemLossService;

  @Operation(description = "Cria um novo item.")
  @RequestBody(description = "Dados do item a ser criado")
  @PreAuthorize("@itemSecurity.hasItemManagementPermission(authentication, #entity.itemTypeId())")
  @PostMapping
  public ResponseEntity<?> createItem(
      @org.springframework.web.bind.annotation.RequestBody ItemRequest entity,
      @Parameter(hidden = true) Authentication authentication) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      Long id = itemService.createItem(entity, lastUser);
      return ResponseEntity.status(201).body(java.util.Map.of("id", id));
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error creating item: " + e.getMessage());
    }
  }

  @Operation(description = "Busca um item pelo ID.")
  @GetMapping("/{id}")
  public ResponseEntity<?> getItem(
      @Parameter(description = "ID do item a ser buscado", example = "1") @PathVariable Long id) {
    try {
      var item = itemService.getItem(id);
      return ResponseEntity.ok(item);
    } catch (NullPointerException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error retrieving item: " + e.getMessage());
    }
  }

  @Operation(description = "Lista todos os itens, com filtros opcionais.")
  @GetMapping
  public ResponseEntity<?> getAllItems(
      @Parameter(description = "ID da seção", example = "2") @RequestParam(required = false)
          Long sectionId,
      @Parameter(description = "ID do tipo de item", example = "3") @RequestParam(required = false)
          Long itemTypeId,
      @Parameter(description = "Se o item está ativo", example = "true")
          @RequestParam(required = false)
          Boolean isActive,
      @Parameter(description = "ID do item", example = "5") @RequestParam(required = false)
          Long itemId,
      @Parameter(description = "Código do item", example = "ABC123") @RequestParam(required = false)
          String itemCode) {
    try {
      var items =
          itemService.getItemsByFilter(
              new ItemFilterCriteria(itemCode, sectionId, itemTypeId, isActive, itemId));
      return ResponseEntity.ok(items);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error retrieving items: " + e.getMessage());
    }
  }

  @Operation(description = "Busca um item pelo código QR criptografado.")
  @GetMapping("/qr")
  public ResponseEntity<?> getItemByQrCode(
      @Parameter(description = "Código QR criptografado", example = "encryptedString") @RequestParam String code) {
      try {
          var item = itemService.getEncryptedItem(code);
          return ResponseEntity.ok(item);
      } catch (IllegalArgumentException e) {
          return ResponseEntity.status(400).body(e.getMessage());
      } catch (NullPointerException e) {
          return ResponseEntity.status(404).body(e.getMessage());
      } catch (Exception e) {
          return ResponseEntity.status(500).body("Error retrieving item: " + e.getMessage());
      }
  }

  @Operation(description = "Atualiza os dados de um item existente.")
  @RequestBody(description = "Novos dados do item")
  @PreAuthorize("@itemSecurity.hasItemManagementPermission(authentication, #id)")
  @PutMapping("/{id}")
  public ResponseEntity<?> updateItem(
      @Parameter(description = "ID do item a ser atualizado", example = "1") @PathVariable Long id,
      @org.springframework.web.bind.annotation.RequestBody ItemRequest entity,
      @Parameter(hidden = true) Authorization authentication) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      itemService.updateItem(id, entity, lastUser);
      return ResponseEntity.ok("Item updated successfully");
    } catch (NullPointerException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error updating item: " + e.getMessage());
    }
  }

  @Operation(description = "Desabilita um item.")
  @PreAuthorize("@itemSecurity.hasItemManagementPermission(authentication, #id)")
  @PatchMapping("/disable/{id}")
  public ResponseEntity<?> disableItem(
      @Parameter(description = "ID do item a ser desabilitado", example = "1") @PathVariable
          Long id,
      @Parameter(hidden = true) Authorization authentication) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      itemService.disableItem(id, lastUser);
      return ResponseEntity.ok("Item disabled successfully");
    } catch (NullPointerException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error disabling item: " + e.getMessage());
    }
  }

  @Operation(description = "Arquiva um item.")
  @RequestBody(description = "Dados do arquivo")
  @PatchMapping("/archive/{id}")
  public ResponseEntity<?> archiveItem(
      @Parameter(description = "ID do item a ser arquivado", example = "1") @PathVariable Long id,
      @org.springframework.web.bind.annotation.RequestBody ArchiveItem archiveItem,
      @Parameter(hidden = true) Authorization authentication) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      itemService.archiveItem(id, archiveItem, lastUser);
      return ResponseEntity.ok("Item archived successfully");
    } catch (NullPointerException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error archiving item: " + e.getMessage());
    }
  }

  @Operation(description = "Cria um registro de perda de item.")
  @RequestBody(description = "Dados da perda do item")
  @PostMapping("/loss")
  public ResponseEntity<?> createItemLoss(
      @org.springframework.web.bind.annotation.RequestBody ItemLossRequest request,
      @Parameter(hidden = true) Authorization authentication) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      itemLossService.createItemLoss(request, lastUser);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error creating item loss: " + e.getMessage());
    }
    return ResponseEntity.status(201).body("Item loss created successfully");
  }

  @Operation(description = "Atualiza um registro de perda de item existente.")
  @RequestBody(description = "Novos dados da perda do item")
  @PutMapping("/loss/{id}")
  public ResponseEntity<?> updateItemLoss(
      @Parameter(description = "ID da perda do item a ser atualizada", example = "1") @PathVariable
          Long id,
      @org.springframework.web.bind.annotation.RequestBody ItemLossRequest request,
      @Parameter(hidden = true) Authorization authentication) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      itemLossService.updateItemLoss(id, request, lastUser);
      return ResponseEntity.ok("Item loss updated successfully");
    } catch (NullPointerException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error updating item loss: " + e.getMessage());
    }
  }

  @Operation(description = "Busca registros de perda de item com base em critérios de filtro.")
  @GetMapping("/loss")
  public ResponseEntity<?> getItemLossByFilter(
      @Parameter(description = "ID do item", example = "2") @RequestParam(required = false)
          Long itemId,
      @Parameter(description = "ID do usuário que registrou a perda", example = "3")
          @RequestParam(required = false)
          Long recordedById,
      @Parameter(description = "Data de início para o filtro", example = "2023-01-01T00:00:00")
          @RequestParam(required = false)
          LocalDateTime startDate,
      @Parameter(description = "Data de fim para o filtro", example = "2023-12-31T23:59:59")
          @RequestParam(required = false)
          LocalDateTime endDate,
      @Parameter(description = "ID da perda do item", example = "5") @RequestParam(required = false)
          Long itemLossId) {
    try {
      var itemLosses =
          itemLossService.getItemLossByFilter(
              new ItemLossFilterCriteria(itemId, recordedById, startDate, endDate, itemLossId));
      return ResponseEntity.ok(itemLosses);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error retrieving item losses: " + e.getMessage());
    }
  }
}
