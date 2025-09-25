package com.codenine.managementservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

import com.codenine.managementservice.dto.itemType.ItemTypeFilterCriteria;
import com.codenine.managementservice.dto.itemType.ItemTypeRequest;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.service.ItemTypeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/item-types")
public class ItemTypeController {

  @Autowired private ItemTypeService itemTypeService;

  /**
   * Cria um novo tipo de item.
   *
   * @param newItemType Dados do tipo de item a ser criado.
   * @return Mensagem de sucesso ou erro.
   */
  @Operation(description = "Cria um novo tipo de item.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "Dados do tipo de item a ser criado")
  @PostMapping
  public ResponseEntity<?> createItemType(@RequestBody ItemTypeRequest newItemType) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      itemTypeService.createItemType(newItemType, lastUser);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error creating item type: " + e.getMessage());
    }
    return ResponseEntity.status(201).body("Item type created successfully");
  }

  /**
   * Busca um tipo de item pelo ID.
   *
   * @param id ID do tipo de item.
   * @return Dados do tipo de item ou mensagem de erro.
   */
  @Operation(description = "Busca um tipo de item pelo ID.")
  @GetMapping("/{id}")
  public ResponseEntity<?> getItemType(
      @Parameter(description = "ID do tipo de item a ser buscado", example = "1") @PathVariable
          Long id) {
    try {
      var itemType = itemTypeService.getItemType(id);
      return ResponseEntity.ok(itemType);
    } catch (NullPointerException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error retrieving item type: " + e.getMessage());
    }
  }

  /**
   * Lista todos os tipos de item, com filtros opcionais.
   *
   * @param itemTypeId ID do tipo de item (opcional)
   * @param sectionId ID da seção (opcional)
   * @param lastUserId ID do último usuário (opcional)
   * @return Lista de tipos de item.
   */
  @Operation(description = "Lista todos os tipos de item, com filtros opcionais.")
  @GetMapping
  public ResponseEntity<?> getAllItemTypes(
      @Parameter(description = "ID do tipo de item", example = "1") @RequestParam(required = false)
          Long itemTypeId,
      @Parameter(description = "ID da seção", example = "2") @RequestParam(required = false)
          Long sectionId,
      @Parameter(description = "ID do último usuário", example = "3")
          @RequestParam(required = false)
          Long lastUserId) {
    try {
      var itemTypes =
          itemTypeService.getItemTypesByFilter(
              new ItemTypeFilterCriteria(itemTypeId, sectionId, lastUserId));
      return ResponseEntity.ok(itemTypes);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error retrieving item types: " + e.getMessage());
    }
  }

  /**
   * Atualiza um tipo de item existente.
   *
   * @param id ID do tipo de item.
   * @param newItemType Novos dados do tipo de item.
   * @return Mensagem de sucesso ou erro.
   */
  @Operation(description = "Atualiza um tipo de item existente.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Novos dados do tipo de item")
  @PutMapping("/{id}")
  public ResponseEntity<?> updateItemType(
      @Parameter(description = "ID do tipo de item a ser atualizado", example = "1") @PathVariable
          Long id,
      @RequestBody ItemTypeRequest newItemType) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      itemTypeService.updateItemType(id, newItemType, lastUser);
      return ResponseEntity.ok("Item type updated successfully");
    } catch (NullPointerException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error updating item type: " + e.getMessage());
    }
  }

  /**
   * Desabilita um tipo de item.
   *
   * @param id ID do tipo de item.
   * @return Mensagem de sucesso ou erro.
   */
  @Operation(description = "Desabilita um tipo de item.")
  @PatchMapping("/disable/{id}")
  public ResponseEntity<?> disableItemType(
      @Parameter(description = "ID do tipo de item a ser desabilitado", example = "1") @PathVariable
          Long id) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      itemTypeService.disableItemType(id, lastUser);
      return ResponseEntity.ok("Item type disabled successfully");
    } catch (NullPointerException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error disabling item type: " + e.getMessage());
    }
  }
}
