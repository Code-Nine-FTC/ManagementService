package com.codenine.managementservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.dto.order.OrderRequest;
import com.codenine.managementservice.dto.order.OrderResponse;
import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.entity.Order;
import com.codenine.managementservice.entity.OrderItem;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.ItemRepository;
import com.codenine.managementservice.repository.OrderItemRepositorio;
import com.codenine.managementservice.repository.OrderRepository;
import com.codenine.managementservice.repository.SupplierCompanyRepository;
import com.codenine.managementservice.repository.UserRepository;
import com.codenine.managementservice.utils.mapper.OrderMapper;

import jakarta.persistence.EntityNotFoundException;

@Service
public class OrderService {

  @Autowired private OrderRepository orderRepository;

  @Autowired private ItemRepository itemRepository;

  @Autowired private SupplierCompanyRepository supplierCompanyRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private OrderItemRepositorio orderItemRepositorio;

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

  public void deleteOrder(Long orderId) {
    Order order = getOrderById(orderId);
    order.getOrderItems().forEach(oi -> orderItemRepositorio.delete(oi));
    orderRepository.delete(order);
    orderRepository.save(null);
  }

  public List<OrderResponse> getAllOrders(Long userId, OrderStatu status) {
    return orderRepository.findAllOrderResponses(userId, status);
  }

  private Order getOrderById(Long id) {
    return orderRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Ordem com ID " + id + " não encontrada."));
  }

  private OrderItem getOrderItemById(Long id) {
    return orderItemRepositorio
        .findById(id)
        .orElseThrow(
            () -> new EntityNotFoundException("Item de ordem com ID " + id + " não encontrado."));
  }
}
