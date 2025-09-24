package com.codenine.managementservice.controller;

import com.codenine.managementservice.dto.order.OrderRequest;
import com.codenine.managementservice.dto.order.OrderResponse;
import com.codenine.managementservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
     * @param request Dados do pedido a ser criado.
     * @return Dados do pedido criado.
     */
    @Operation(description = "Cria um novo pedido.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do pedido a ser criado")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
        @org.springframework.web.bind.annotation.RequestBody OrderRequest request) {
        OrderResponse response = orderService.toOrderResponse(orderService.createOrder(request));
        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza o status de um pedido.
     * @param id ID do pedido.
     * @param body Corpo contendo o novo status.
     * @return Dados do pedido atualizado.
     */
    @Operation(description = "Atualiza o status de um pedido.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Novo status do pedido (campo 'status')")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
        @Parameter(description = "ID do pedido a ser atualizado", example = "1")
        @PathVariable Long id,
        @org.springframework.web.bind.annotation.RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        OrderResponse response = orderService.toOrderResponse(orderService.updateOrderStatus(id, newStatus));
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todos os pedidos.
     * @return Lista de pedidos.
     */
    @Operation(description = "Lista todos os pedidos.")
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Busca um pedido pelo ID.
     * @param id ID do pedido.
     * @return Dados do pedido ou 404 se não encontrado.
     */
    @Operation(description = "Busca um pedido pelo ID.")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
        @Parameter(description = "ID do pedido a ser buscado", example = "1")
        @PathVariable Long id) {
        try {
            OrderResponse response = orderService.getOrderById(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
