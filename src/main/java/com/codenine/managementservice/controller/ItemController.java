package com.codenine.managementservice.controller;

import org.springframework.web.bind.annotation.RestController;

import com.codenine.managementservice.dto.ItemFilterCriteria;
import com.codenine.managementservice.dto.ItemLossRequest;
import com.codenine.managementservice.dto.ItemRequest;
import com.codenine.managementservice.service.ItemLossService;
import com.codenine.managementservice.service.ItemService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/items")
public class ItemController {
    
    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemLossService itemLossService;

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

    @GetMapping("/all")
    public ResponseEntity<?> getAllItems(@RequestBody ItemFilterCriteria filters) {
        try {
            var items = itemService.getItemsByFilter(filters);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving items: " + e.getMessage());
        }
    }

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

    @PatchMapping("/archive/{id}")
    public ResponseEntity<?> archiveItem(@PathVariable Long id) {
        try {
            itemService.archiveItem(id);
            return ResponseEntity.ok("Item archived successfully");
        } catch (NullPointerException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error archiving item: " + e.getMessage());
        }
    }

    @PostMapping("/loss")
    public ResponseEntity<?> createItemLoss(@RequestBody ItemLossRequest request) {
        try {
            itemLossService.createItemLoss(request);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating item loss: " + e.getMessage());
        }
        return ResponseEntity.status(201).body("Item loss created successfully");
    }

    @PutMapping("/loss/{id}")
    public ResponseEntity<?> updateItemLoss(@PathVariable Long id, @RequestBody ItemLossRequest request) {
        try {
            itemLossService.updateItemLoss(id, request);
            return ResponseEntity.ok("Item loss updated successfully");
        } catch (NullPointerException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating item loss: " + e.getMessage());
        }
    }

}
