package com.codenine.managementservice.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.codenine.managementservice.dto.order.OrderFilterCriteria;
import com.codenine.managementservice.dto.order.OrderItemResponse;
import com.codenine.managementservice.dto.order.OrderRequest;
import com.codenine.managementservice.dto.order.OrderResponse;
import com.codenine.managementservice.dto.order.OrderStatus;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/orders")
public class OrderController {

  @Autowired
  private OrderService orderService;


  @Operation(description = "Cria um novo pedido.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do pedido a ser criado")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @PostMapping
  public ResponseEntity<?> createOrder(
      @org.springframework.web.bind.annotation.RequestBody OrderRequest request,
      Authorization authorization) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      orderService.createOrder(request, lastUser);
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.unprocessableEntity().build();
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> updateOrder(
      @PathVariable Long id,
      @RequestBody OrderRequest request,
      Authorization authorization) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      orderService.updateOrder(id, request, lastUser);
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.unprocessableEntity().build();
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }


  @Operation(description = "Lista todos os pedidos com filtros opcionais.")
  @GetMapping
  public ResponseEntity<List<OrderResponse>> getAllOrders(
      @Parameter(description = "ID específico do pedido", example = "1") @RequestParam(required = false) Long orderId,
      @Parameter(description = "Status do pedido", example = "PENDING") @RequestParam(required = false) OrderStatus status,
      @Parameter(description = "Id do fornecedor", example = "1") @RequestParam(required = false) Long supplierId,
      @Parameter(description = "Id da seção", example = "1") @RequestParam(required = false) Long sectionId) {
    try {
      List<OrderResponse> responses = orderService.getAllOrders(
          new OrderFilterCriteria(orderId, status, supplierId, sectionId));
      return ResponseEntity.ok(responses);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.unprocessableEntity().build();
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  @Operation(description = "Busca um pedido pelo ID.")
  @GetMapping("/{id}")
  public ResponseEntity<OrderResponse> getOrderById(
      @Parameter(description = "ID do pedido a ser buscado", example = "1") @PathVariable Long id) {
    try {
      OrderResponse response = orderService.getOrderResponseById(id);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.unprocessableEntity().build();
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping("/items/{id}")
  public ResponseEntity<List<OrderItemResponse>> getOrderItemsByOrderId(
      @Parameter(description = "ID do pedido", example = "1") @PathVariable Long id) {
    try {
      List<OrderItemResponse> responses = orderService.getOrderItemsByOrderId(id);
      return ResponseEntity.ok(responses);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.unprocessableEntity().build();
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }


  @Operation(description = "Remove um pedido pelo ID.")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @PatchMapping("/cancel/{id}")
  public ResponseEntity<Void> deleteOrder(
      @Parameter(description = "ID do pedido a ser removido", example = "1") @PathVariable Long id,
      Authorization authorization) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      orderService.cancelOrder(id, lastUser);
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.unprocessableEntity().build();
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @PatchMapping("/approve/{id}")
  public ResponseEntity<Void> approveOrder(
      @Parameter(description = "ID do pedido a ser aprovado", example = "1") @PathVariable Long id,
      Authorization authorization) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      orderService.approveOrder(id, lastUser);
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.unprocessableEntity().build();
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }


  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @PatchMapping("/process/{id}")
  public ResponseEntity<Void> processOrder(
      @Parameter(description = "ID do pedido a ser processado", example = "1") @PathVariable Long id,
      Authorization authorization) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      orderService.processOrder(id, lastUser);
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.unprocessableEntity().build();
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @PatchMapping("/complete/{id}")
  public ResponseEntity<Void> completeOrder(
      @Parameter(description = "ID do pedido a ser completado", example = "1") @PathVariable Long id,
      @RequestBody LocalDateTime withdrawDay,
      Authorization authorization) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      orderService.completeOrder(id, lastUser, withdrawDay);
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.unprocessableEntity().build();
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }
}
