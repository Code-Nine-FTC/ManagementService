package com.codenine.managementservice.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codenine.managementservice.dto.order.OrderRequest;
import com.codenine.managementservice.dto.order.OrderResponse;
import com.codenine.managementservice.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/orders")
public class OrderController {
  private final OrderService orderService;

  @Autowired
  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

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
  public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
    OrderResponse response = orderService.toOrderResponse(orderService.createOrder(request));
    return ResponseEntity.ok(response);
  }

  /**
   * Atualiza o status de um pedido.
   *
   * @param id ID do pedido.
   * @param body Corpo contendo o novo status.
   * @return Dados do pedido atualizado.
   */
  @Operation(description = "Atualiza o status de um pedido.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "Novo status do pedido (campo 'status')")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @PatchMapping("/{id}/status")
  public ResponseEntity<OrderResponse> updateOrderStatus(
      @Parameter(description = "ID do pedido a ser atualizado", example = "1") @PathVariable
          Long id,
      @RequestBody Map<String, String> body) {
    String newStatus = body.get("status");
    OrderResponse response =
        orderService.toOrderResponse(orderService.updateOrderStatus(id, newStatus));
    return ResponseEntity.ok(response);
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
          String status,
      @Parameter(description = "ID do usuário que criou o pedido", example = "1")
          @RequestParam(required = false)
          Long createdById,
      @Parameter(description = "ID do usuário que fez a última modificação", example = "1")
          @RequestParam(required = false)
          Long lastUserId) {
    List<OrderResponse> orders =
        orderService.getOrdersWithFilters(orderId, status, createdById, lastUserId);
    return ResponseEntity.ok(orders);
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
      OrderResponse response = orderService.getOrderById(id);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }
}
