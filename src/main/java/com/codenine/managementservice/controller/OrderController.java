package com.codenine.managementservice.controller;

import java.util.List;
import java.util.Map;

import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.codenine.managementservice.dto.order.OrderFilterCriteria;
import com.codenine.managementservice.dto.order.OrderRequest;
import com.codenine.managementservice.dto.order.OrderResponse;
import com.codenine.managementservice.dto.order.OrderStatus;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/orders")
public class OrderController {

  @Autowired
  private OrderService orderService;


  /**
   * Cria um novo pedido.
   *
   * @param request Dados do pedido a ser criado.
   * @return Dados do pedido criado.
   */
  @Operation(description = "Cria um novo pedido.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "Dados do pedido a ser criado")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @PostMapping
  public ResponseEntity<?> createOrder(
      @org.springframework.web.bind.annotation.RequestBody OrderRequest request, Authorization authorization) {
        try{
          User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
          orderService.createOrder(request, lastUser);
                return ResponseEntity.ok().build();
    }  catch (IllegalArgumentException e) {
      return ResponseEntity.unprocessableEntity().build();
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Atualiza o  pedido.
   *
   * @param id ID do pedido.
   * @param body Corpo contendo o novo status.
   * @return Dados do pedido atualizado.
   */
  @Operation(description = "Atualiza o Pedido.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "Novo status do pedido (campo 'status')")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @PatchMapping("/{id}/status")
  public ResponseEntity<?> updateOrderStatus(
      @Parameter(description = "ID do pedido a ser atualizado", example = "1") @PathVariable
          Long id,
      @RequestBody OrderRequest body, Authorization authorization) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      orderService.updateOrder(id, body, lastUser);
            return ResponseEntity.ok().build();
    }  catch (IllegalArgumentException e) {
      return ResponseEntity.unprocessableEntity().build();
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Lista todos os pedidos com filtros opcionais.
   *
   * @param orderId ID específico do pedido (opcional)
   * @param status Status do pedido (opcional)
   * @param createdById ID do usuário que criou o pedido (opcional)
   * @param lastUserId ID do usuário que fez a última modificação (opcional)
   * @return Lista de pedidos.
   */
  @Operation(description = "Lista todos os pedidos com filtros opcionais.")
  @GetMapping
  public ResponseEntity<List<OrderResponse>> getAllOrders(
      @Parameter(description = "ID específico do pedido", example = "1")
          @RequestParam(required = false)
          Long orderId,
      @Parameter(description = "Status do pedido", example = "PENDING")
          @RequestParam(required = false)
          OrderStatus status,
      @Parameter(description = "Id do fornecedor", example = "1")
          @RequestParam(required = false)
          Long supplierId) {
    try {
      List<OrderResponse> responses =
          orderService.getAllOrders(new OrderFilterCriteria(orderId, status, supplierId));
            return ResponseEntity.ok(responses);
    }  catch (IllegalArgumentException e) {
      return ResponseEntity.unprocessableEntity().build();
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Busca um pedido pelo ID.
   *
   * @param id ID do pedido.
   * @return Dados do pedido ou 404 se não encontrado.
   */
  @Operation(description = "Busca um pedido pelo ID.")
  @GetMapping("/{id}")
  public ResponseEntity<OrderResponse> getOrderById(
      @Parameter(description = "ID do pedido a ser buscado", example = "1") @PathVariable Long id) {
    try {
      OrderResponse response = orderService.getOrderByIdResponse(id);
           return ResponseEntity.ok(response);
    }  catch (IllegalArgumentException e) {
      return ResponseEntity.unprocessableEntity().build();
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Remove um pedido pelo ID.
   *
   * @param id ID do pedido.
   * @return Sem conteúdo em caso de sucesso.
   */
  @Operation(description = "Remove um pedido pelo ID.")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @PatchMapping("/cancel/{id}")
  public ResponseEntity<Void> deleteOrder(
      @Parameter(description = "ID do pedido a ser removido", example = "1") @PathVariable
          Long id, Authorization authorization) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      orderService.deleteOrder(id, lastUser);
      return ResponseEntity.ok().build();
    }  catch (IllegalArgumentException e) {
      return ResponseEntity.unprocessableEntity().build();
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Aprova um pedido pelo ID.
   *
   * @param id ID do pedido.
   * @return 200 OK em caso de sucesso.
   */
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @PatchMapping("/approve/{id}")
  public ResponseEntity<Void> approveOrder(
      @Parameter(description = "ID do pedido a ser aprovado", example = "1") @PathVariable
          Long id, Authorization authorization) {
    try {
      User lastUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      orderService.orderApproved(id, lastUser);
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.unprocessableEntity().build();
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }
}
