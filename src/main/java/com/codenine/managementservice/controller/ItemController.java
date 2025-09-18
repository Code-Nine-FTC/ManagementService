package com.codenine.managementservice.controller;

import com.codenine.managementservice.exception.UserSectionMismatchException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.codenine.managementservice.dto.ItemFilterCriteria;
// import com.codenine.managementservice.dto.ItemLossRequest;
import com.codenine.managementservice.dto.ItemRequest;
// import com.codenine.managementservice.service.ItemLossService;
import com.codenine.managementservice.service.ItemService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;


@RestController
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    // @Autowired
    // private ItemLossService itemLossService;

    @PreAuthorize("@itemSecurity.hasItemManagementPermission(authentication, #entity.itemTypeId())")
    @PostMapping("/")
    public ResponseEntity<String> createItem(@RequestBody ItemRequest entity) {
        try {
            itemService.createItem(entity);
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
            @RequestParam(required = false) Long itemId
    ) {
        try {
            ItemFilterCriteria filters = new ItemFilterCriteria(
                    supplierId, sectionId, itemTypeId, lastUserId, isActive, itemId
            );
            var items = itemService.getItemsByFilter(filters);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving items: " + e.getMessage());
        }
    }

    @PreAuthorize("@itemSecurity.hasItemManagementPermission(authentication, #entity.typeItemId())")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @RequestBody ItemRequest entity) {
        try {
            itemService.updateItem(id, entity);
            return ResponseEntity.ok("Item updated successfully");
        }catch (NullPointerException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
         catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating item: " + e.getMessage());
        }
    }

    @PreAuthorize("@itemSecurity.hasItemManagementPermission(authentication, #entity.typeItemId())")
    @PatchMapping("/disable/{id}")
    public ResponseEntity<?> disableItem(@PathVariable Long id) {
        try {
            itemService.disableItem(id);
            return ResponseEntity.ok("Item disabled successfully");
        } catch (NullPointerException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error disabling item: " + e.getMessage());
        }
    }

    // @PostMapping("/loss")
    // public ResponseEntity<?> createItemLoss(@RequestBody ItemLossRequest request) {
    //     try {
    //         itemLossService.createItemLoss(request);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(500).body("Error creating item loss: " + e.getMessage());
    //     }
    //     return ResponseEntity.status(201).body("Item loss created successfully");
    // }

    // @PutMapping("/loss/{id}")
    // public ResponseEntity<?> updateItemLoss(@PathVariable Long id, @RequestBody ItemLossRequest request) {
    //     try {
    //         itemLossService.updateItemLoss(id, request);
    //         return ResponseEntity.ok("Item loss updated successfully");
    //     } catch (NullPointerException e) {
    //         return ResponseEntity.status(404).body(e.getMessage());
    //     } catch (Exception e) {
    //         return ResponseEntity.status(500).body("Error updating item loss: " + e.getMessage());
    //     }
    // }

}
