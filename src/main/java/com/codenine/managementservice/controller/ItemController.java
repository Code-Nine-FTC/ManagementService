package com.codenine.managementservice.controller;

import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codenine.managementservice.dto.item.ArchiveItem;
import com.codenine.managementservice.dto.item.ItemFilterCriteria;
import com.codenine.managementservice.dto.item.ItemRequest;
import com.codenine.managementservice.dto.itemLoss.ItemLossRequest;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.service.ItemLossService;
import com.codenine.managementservice.service.ItemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/items")
public class ItemController {

  @Autowired private ItemService itemService;

  @Autowired private ItemLossService itemLossService;

  /**
   * Cria um novo item.
   *
   * @param entity Dados do item a ser criado.
   * @return Mensagem de sucesso ou erro.
   */
  @Operation(description = "Cria um novo item.")
  @PreAuthorize("@itemSecurity.hasItemManagementPermission(authentication, #entity.itemTypeId())")
  @PostMapping
  public ResponseEntity<String> createItem(
      @RequestBody ItemRequest entity, @Parameter(hidden = true) Authentication authentication) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      itemService.createItem(entity, lastUser);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error creating item: " + e.getMessage());
    }
    return ResponseEntity.status(201).body("Item created successfully");
  }

  /**
   * Busca um item pelo ID.
   *
   * @param id ID do item.
   * @return Dados do item ou mensagem de erro.
   */
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

  /**
   * Lista todos os itens, com filtros opcionais.
   *
   * @param supplierId ID do fornecedor (opcional)
   * @param sectionId ID da seção (opcional)
   * @param itemTypeId ID do tipo de item (opcional)
   * @param lastUserId ID do último usuário (opcional)
   * @param isActive Se o item está ativo (opcional)
   * @param itemId ID do item (opcional)
   * @return Lista de itens.
   */
  @Operation(description = "Lista todos os itens, com filtros opcionais.")
  @GetMapping
  public ResponseEntity<?> getAllItems(
      @Parameter(description = "ID do fornecedor", example = "1") @RequestParam(required = false)
          Long supplierId,
      @Parameter(description = "ID da seção", example = "2") @RequestParam(required = false)
          Long sectionId,
      @Parameter(description = "ID do tipo de item", example = "3") @RequestParam(required = false)
          Long itemTypeId,
      @Parameter(description = "ID do último usuário", example = "4")
          @RequestParam(required = false)
          Long lastUserId,
      @Parameter(description = "Se o item está ativo", example = "true")
          @RequestParam(required = false)
          Boolean isActive,
      @Parameter(description = "ID do item", example = "5") @RequestParam(required = false)
          Long itemId) {
    try {
      var items =
          itemService.getItemsByFilter(
              new ItemFilterCriteria(
                  supplierId, sectionId, itemTypeId, lastUserId, isActive, itemId));
      return ResponseEntity.ok(items);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error retrieving items: " + e.getMessage());
    }
  }

  /**
   * Atualiza os dados de um item existente.
   *
   * @param id ID do item a ser atualizado.
   * @param entity Novos dados do item.
   * @return Mensagem de sucesso ou erro.
   */
  @Operation(description = "Atualiza os dados de um item existente.")
  @PreAuthorize("@itemSecurity.hasItemManagementPermission(authentication, #id)")
  @PutMapping("/{id}")
  public ResponseEntity<?> updateItem(
      @Parameter(description = "ID do item a ser atualizado", example = "1") @PathVariable Long id,
      @RequestBody ItemRequest entity,
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

  /**
   * Desabilita um item.
   *
   * @param id ID do item a ser desabilitado.
   * @return Mensagem de sucesso ou erro.
   */
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

  /**
   * Arquiva um item.
   *
   * @param id ID do item a ser arquivado.
   * @param archiveItem Dados do arquivo.
   * @return Mensagem de sucesso ou erro.
   */
  @Operation(description = "Arquiva um item.")
  @PatchMapping("/archive/{id}")
  public ResponseEntity<?> archiveItem(
      @Parameter(description = "ID do item a ser arquivado", example = "1") @PathVariable Long id,
      @RequestBody ArchiveItem archiveItem,
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

  /**
   * Cria um registro de perda de item.
   *
   * @param request Dados da perda do item.
   * @return Mensagem de sucesso ou erro.
   */
  @Operation(description = "Cria um registro de perda de item.")
  @PostMapping("/loss")
  public ResponseEntity<?> createItemLoss(
      @RequestBody ItemLossRequest request,
      @Parameter(hidden = true) Authorization authentication) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      itemLossService.createItemLoss(request, lastUser);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error creating item loss: " + e.getMessage());
    }
    return ResponseEntity.status(201).body("Item loss created successfully");
  }

  /**
   * Atualiza um registro de perda de item existente.
   *
   * @param id ID da perda do item a ser atualizada.
   * @param request Novos dados da perda do item.
   * @return Mensagem de sucesso ou erro.
   */
  @Operation(description = "Atualiza um registro de perda de item existente.")
  @PutMapping("/loss/{id}")
  public ResponseEntity<?> updateItemLoss(
      @Parameter(description = "ID da perda do item a ser atualizada", example = "1") @PathVariable
          Long id,
      @RequestBody ItemLossRequest request,
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
}
