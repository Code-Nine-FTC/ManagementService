package com.codenine.managementservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.dto.item.ItemResponse;
import com.codenine.managementservice.dto.order.OrderRequest;
import com.codenine.managementservice.dto.order.OrderResponse;
import com.codenine.managementservice.dto.supplier.SupplierCompanyResponse;
import com.codenine.managementservice.dto.user.UserRequest;
import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.entity.Order;
import com.codenine.managementservice.entity.Section;
import com.codenine.managementservice.entity.SupplierCompany;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.ItemRepository;
import com.codenine.managementservice.repository.OrderItemRepositorio;
import com.codenine.managementservice.repository.OrderRepository;
import com.codenine.managementservice.repository.SupplierCompanyRepository;
import com.codenine.managementservice.repository.UserRepository;
import com.codenine.managementservice.utils.mapper.OrderMapper;
import com.codenine.managementservice.entity.OrderItem;

import jakarta.persistence.EntityNotFoundException;

@Service
public class OrderService {

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private SupplierCompanyRepository supplierCompanyRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private OrderItemRepositorio orderItemRepositorio;

  public void createOrder(OrderRequest request, User lastUser) {
    List<Item> items = itemRepository.findAllById(request.itemQuantities().keySet());
    if (items.size() != request.itemQuantities().keySet().size())
      throw new IllegalArgumentException("Um ou mais IDs de item são inválidos.");

    OrderItem orderItem = new OrderItem();
    Object[] entities = OrderMapper.toEntity(request, lastUser, orderItem, items);
    Order order = (Order) entities[0];
    orderItem = (OrderItem) entities[1];

    orderRepository.save(order);
    orderItemRepositorio.save(orderItem);
  }

}
