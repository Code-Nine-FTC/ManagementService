package com.codenine.managementservice.controller;

import org.springframework.web.bind.annotation.RestController;

import com.codenine.managementservice.dto.itemType.ItemTypeFilterCriteria;
import com.codenine.managementservice.dto.itemType.ItemTypeRequest;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.service.ItemTypeService;

import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;

@RestController("/item-types")
public class ItemTypeController {

    @Autowired
    private ItemTypeService itemTypeService;

    @PostMapping("/")
    public ResponseEntity<?> createItemType(@RequestBody ItemTypeRequest newItemType, Authorization authentication) {
        try {
            User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            itemTypeService.createItemType(newItemType, lastUser);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating item type: " + e.getMessage());
        }
        return ResponseEntity.status(201).body("Item type created successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getItemType(@PathVariable Long id) {
        try {
            var itemType = itemTypeService.getItemType(id);
            return ResponseEntity.ok(itemType);
        } catch (NullPointerException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving item type: " + e.getMessage());
        }
    }

    @GetMapping("/")
    public ResponseEntity<?> getAllItemTypes(
            @RequestParam(required = false) Long itemTypeId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long lastUserId) {
        try {
            var itemTypes = itemTypeService.getItemTypesByFilter(
                    new ItemTypeFilterCriteria(itemTypeId, sectionId, lastUserId));
            return ResponseEntity.ok(itemTypes);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving item types: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItemType(@PathVariable Long id, @RequestBody ItemTypeRequest entity,
            Authorization authentication) {
        try {
            User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            itemTypeService.updateItemType(id, entity, lastUser);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating item type: " + e.getMessage());
        }
        return ResponseEntity.status(200).body("Item type updated successfully");
    }

    @PatchMapping("/disable/{id}")
    public ResponseEntity<?> disableItemType(@PathVariable Long id, Authorization authentication) {
        try {
            User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            itemTypeService.disableItemType(id, lastUser);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error disabling item type: " + e.getMessage());
        }
        return ResponseEntity.status(200).body("Item type disabled successfully");
    }
}
