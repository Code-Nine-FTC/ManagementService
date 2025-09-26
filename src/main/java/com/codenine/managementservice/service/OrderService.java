package com.codenine.managementservice.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.dto.order.OrderFilterCriteria;
import com.codenine.managementservice.dto.order.OrderRequest;
import com.codenine.managementservice.dto.order.OrderResponse;
import com.codenine.managementservice.dto.order.OrderStatus;
import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.entity.Order;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.ItemRepository;
import com.codenine.managementservice.repository.OrderRepository;

import com.codenine.managementservice.utils.mapper.OrderMapper;

import jakarta.persistence.EntityNotFoundException;

@Service
public class OrderService {

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private ItemRepository itemRepository;

  public void createOrder(OrderRequest request, User lastUser) {
    List<Item> items = itemRepository.findAllById(request.itemQuantities().keySet());
    if (items.size() != request.itemQuantities().keySet().size())
      throw new IllegalArgumentException("Um ou mais IDs de item são inválidos.");

    Order order = OrderMapper.toEntity(request, lastUser, items);

    orderRepository.save(order);
  }

  public void updateOrder(Long orderId, OrderRequest request, User lastUser) {
    Order order = getOrderById(orderId);
    List<Item> items = itemRepository.findAllById(request.itemQuantities().keySet());
    if (items.size() != request.itemQuantities().keySet().size())
      throw new IllegalArgumentException("Um ou mais IDs de item são inválidos.");

    order = OrderMapper.toUpdate(order, request, lastUser, items);

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
        criteria.userId() != null ? criteria.userId() : null,
        criteria.status() != null ? criteria.status().name() : null,
        criteria.supplierId() != null ? criteria.supplierId() : null);
  }

  public OrderResponse getOrderResponseById(Long orderId) {
    return orderRepository.findAllOrderResponses(orderId, null, orderId).stream().findFirst()
        .orElseThrow(() -> new EntityNotFoundException("Ordem com ID " + orderId + " não encontrada."));
  }

  public void approveOrder(Long orderId, User lastUser) {
    Order order = getOrderById(orderId);
    order.setStatus(OrderStatus.APPROVED.name());
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
