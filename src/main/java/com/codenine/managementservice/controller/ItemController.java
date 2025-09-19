package com.codenine.managementservice.controller;

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
import com.codenine.managementservice.dto.itemLoss.ItemLossRequest;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.service.ItemLossService;
import com.codenine.managementservice.service.ItemService;

@RestController
@RequestMapping("/items")
public class ItemController {

  @Autowired private ItemService itemService;

  @Autowired private ItemLossService itemLossService;

  @PreAuthorize("@itemSecurity.hasItemManagementPermission(authentication, #entity.itemTypeId())")
  @PostMapping("/")
  public ResponseEntity<String> createItem(
      @RequestBody ItemRequest entity, Authentication authentication) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      itemService.createItem(entity, lastUser);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error creating item: " + e.getMessage());
    }
    return ResponseEntity.status(201).body("Item created successfully");
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getItem(@PathVariable Long id) {
    try {
      var item = itemService.getItem(id);
      return ResponseEntity.ok(item);
    } catch (NullPointerException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error retrieving item: " + e.getMessage());
    }
  }

  @GetMapping("/")
  public ResponseEntity<?> getAllItems(
      @RequestParam(required = false) Long supplierId,
      @RequestParam(required = false) Long sectionId,
      @RequestParam(required = false) Long itemTypeId,
      @RequestParam(required = false) Long lastUserId,
      @RequestParam(required = false) Boolean isActive,
      @RequestParam(required = false) Long itemId) {
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

  @PreAuthorize("@itemSecurity.hasItemManagementPermission(authentication, #id)")
  @PutMapping("/{id}")
  public ResponseEntity<?> updateItem(
      @PathVariable Long id, @RequestBody ItemRequest entity, Authorization authentication) {
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

  @PreAuthorize("@itemSecurity.hasItemManagementPermission(authentication, #id)")
  @PatchMapping("/disable/{id}")
  public ResponseEntity<?> disableItem(@PathVariable Long id, Authorization authentication) {
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

  @PatchMapping("/archive/{id}")
  public ResponseEntity<?> archiveItem(
      @PathVariable Long id, @RequestBody ArchiveItem archiveItem, Authorization authentication) {
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

  @PostMapping("/loss")
  public ResponseEntity<?> createItemLoss(
      @RequestBody ItemLossRequest request, Authorization authentication) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      itemLossService.createItemLoss(request, lastUser);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error creating item loss: " + e.getMessage());
    }
    return ResponseEntity.status(201).body("Item loss created successfully");
  }

  @PutMapping("/loss/{id}")
  public ResponseEntity<?> updateItemLoss(
      @PathVariable Long id, @RequestBody ItemLossRequest request, Authorization authentication) {
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
