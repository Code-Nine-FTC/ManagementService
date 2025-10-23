package com.codenine.managementservice.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codenine.managementservice.dto.purchaseOrder.EmailStatus;
import com.codenine.managementservice.dto.purchaseOrder.PurchaseOrderFilterCriteria;
import com.codenine.managementservice.dto.purchaseOrder.PurchaseOrderRequest;
import com.codenine.managementservice.dto.purchaseOrder.PurchaseOrderResponse;
import com.codenine.managementservice.dto.purchaseOrder.Status;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.service.PurchaseOrderService;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {
    
    private final PurchaseOrderService purchaseOrderService;

    @PostMapping("/")
    public ResponseEntity<?> createPurchaseOrder(@RequestBody PurchaseOrderRequest request, Authorization authorization) {
        try{
            User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            purchaseOrderService.createPurchaseOrder(request, lastUser);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPurchaseOrder(@PathVariable Long id) {
        try {
            PurchaseOrderResponse response = purchaseOrderService.getPurchaseOrderById(id);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllPurchaseOrders(
        @Parameter(description = "Filter by supplier company ID") @RequestParam(required = false) Long supplierCompanyId,
        @Parameter(description = "Filter by order ID") @RequestParam(required = false) Long orderId,
        @Parameter(description = "Filter by status") @RequestParam(required = false) String status,
        @Parameter(description = "Filter by email status") @RequestParam(required = false) String emailStatus,
        @Parameter(description = "Filter by creation date after") @RequestParam(required = false) LocalDateTime createdAfter,
        @Parameter(description = "Filter by creation date before") @RequestParam(required = false) LocalDateTime createdBefore,
        @Parameter(description = "Filter by year") @RequestParam(required = false) Integer year
    ) {
        try {
            
            List<PurchaseOrderResponse> responses = purchaseOrderService.getPurchaseOrders(
                new PurchaseOrderFilterCriteria(
                    supplierCompanyId,
                    orderId,
                    status,
                    emailStatus,
                    createdAfter,
                    createdBefore,
                    year
                )
            );
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePurchaseOrder(@PathVariable Long id, @RequestBody PurchaseOrderRequest request) {
        try {
            User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            purchaseOrderService.updatePurchaseOrder(id, request, lastUser);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updatePurchaseOrderStatus(@PathVariable Long id, @RequestParam Status status, Authorization authorization) {
        try {
            User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            purchaseOrderService.updateOrderStatus(id, status, lastUser);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();  
        }
    }

    @PostMapping("/{id}/send-email")
    public ResponseEntity<?> sendPurchaseOrderEmail(@PathVariable Long id, @RequestParam EmailStatus emailStatus, Authorization authorization) {
        try {
            User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }


}
    }