package com.codenine.managementservice.service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.dto.order.OrderFilterCriteria;
import com.codenine.managementservice.dto.order.OrderItemResponse;
import com.codenine.managementservice.dto.order.OrderRequest;
import com.codenine.managementservice.dto.order.OrderResponse;
import com.codenine.managementservice.dto.order.OrderStatus;
import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.entity.Order;
import com.codenine.managementservice.entity.Section;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.ItemRepository;
import com.codenine.managementservice.repository.OrderRepository;
import com.codenine.managementservice.repository.SectionRepository;
import com.codenine.managementservice.utils.mapper.OrderMapper;

import jakarta.persistence.EntityNotFoundException;

@Service
public class OrderService {

  @Autowired private OrderRepository orderRepository;

  @Autowired private ItemRepository itemRepository;

  @Autowired private SectionRepository sectionRepository;

  // SupplierCompany linkage removed

  public Long createOrder(OrderRequest request, User lastUser) {
    // Validar número do pedido
    if (request.orderNumber() == null || request.orderNumber().isBlank()) {
      throw new IllegalArgumentException("orderNumber é obrigatório");
    }
    if (orderRepository.existsByOrderNumber(request.orderNumber())) {
      // Usaremos IllegalStateException para diferenciar no controller e retornar 409
      throw new IllegalStateException("Número do pedido já existente");
    }

    List<Item> items = itemRepository.findAllById(request.itemQuantities().keySet());
    if (items.size() != request.itemQuantities().keySet().size())
      throw new IllegalArgumentException("Um ou mais IDs de item são inválidos.");
    // Resolve seção: prioridade para sectionId do request; caso contrário, fallback para a primeira seção do usuário
    Section section = null;
    if (request.sectionId() != null) {
      section = sectionRepository.findById(request.sectionId()).orElse(null);
    } else if (lastUser.getSections() != null && !lastUser.getSections().isEmpty()) {
      Long sectionId = lastUser.getSections().get(0).getId();
      section = sectionRepository.findById(sectionId).orElse(null);
    }

    Order order = OrderMapper.toEntity(request, lastUser, items, section);
    // withdrawDay (yyyy-MM-dd) opcional
    if (request.withdrawDay() != null && !request.withdrawDay().isBlank()) {
      LocalDate d = LocalDate.parse(request.withdrawDay());
      order.setWithdrawDay(d.atStartOfDay());
    }

    Order saved = orderRepository.save(order);
    return saved.getId();
  }

  public void updateOrder(Long orderId, OrderRequest request, User lastUser) {
    Order order = getOrderById(orderId);
    // Não permitimos alteração do número do pedido neste momento
    // Se enviado e diferente, rejeita
    if (request.orderNumber() != null && !request.orderNumber().isBlank()
        && (order.getOrderNumber() == null || !order.getOrderNumber().equals(request.orderNumber()))) {
      throw new IllegalArgumentException("Alteração do número do pedido não é permitida.");
    }

    if (request.itemQuantities() != null && !request.itemQuantities().isEmpty()) {
      List<Item> items = itemRepository.findAllById(request.itemQuantities().keySet());
      if (items.size() != request.itemQuantities().keySet().size())
        throw new IllegalArgumentException("Um ou mais IDs de item são inválidos.");
      order = OrderMapper.toUpdate(order, request, lastUser, items);
    }

    if (request.withdrawDay() != null && !request.withdrawDay().isBlank()) {
      LocalDate d = LocalDate.parse(request.withdrawDay());
      order.setWithdrawDay(d.atStartOfDay());
      order.setLastUser(lastUser);
      order.setLastUpdate(LocalDateTime.now());
    }

    if (request.sectionId() != null) {
      Section section = sectionRepository.findById(request.sectionId()).orElse(null);
      order.setSection(section);
      order.setLastUser(lastUser);
      order.setLastUpdate(LocalDateTime.now());
    }

    orderRepository.save(order);
  }

  public void cancelOrder(Long orderId, User lastUser) {
    Order order = getOrderById(orderId);
    order.setStatus(OrderStatus.CANCELLED.name());
    order.setLastUser(lastUser);
    order.setLastUpdate(LocalDateTime.now());
    orderRepository.save(order);
  }

  public List<OrderResponse> getAllOrders(OrderFilterCriteria criteria) {
    return orderRepository.findAllOrderResponses(
        criteria.orderId() != null ? criteria.orderId() : null,
        criteria.status() != null ? criteria.status().name() : null,
        criteria.sectionId() != null ? criteria.sectionId() : null);
  }

  public List<OrderItemResponse> getOrderItemsByOrderId(Long orderId) {
    getOrderById(orderId);
    return orderRepository.findAllOrderItemResponsesByOrderId(orderId);
  }

  public OrderResponse getOrderResponseById(Long orderId) {
    return orderRepository.findAllOrderResponses(orderId, null, null).stream()
        .findFirst()
        .orElseThrow(
            () -> new EntityNotFoundException("Ordem com ID " + orderId + " não encontrada."));
  }

  public void approveOrder(Long orderId, User lastUser) {
    Order order = getOrderById(orderId);
    order.setStatus(OrderStatus.APPROVED.name());
    order.setLastUser(lastUser);
    order.setLastUpdate(LocalDateTime.now());
    orderRepository.save(order);
  }

  public void processOrder(Long orderId, User lastUser) {
    Order order = getOrderById(orderId);
    order.setStatus(OrderStatus.PROCESSING.name());
    order.setLastUser(lastUser);
    order.setLastUpdate(LocalDateTime.now());
    orderRepository.save(order);
  }

  public void completeOrder(Long orderId, User lastUser, LocalDateTime withdrawDay) {
    Order order = getOrderById(orderId);
    order.setStatus(OrderStatus.COMPLETED.name());
    order.setWithdrawDay(withdrawDay != null ? withdrawDay : LocalDateTime.now());
    order.setLastUser(lastUser);
    order.setLastUpdate(LocalDateTime.now());
    orderRepository.save(order);
  }

  private Order getOrderById(Long id) {
    return orderRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Ordem com ID " + id + " não encontrada."));
  }
}
