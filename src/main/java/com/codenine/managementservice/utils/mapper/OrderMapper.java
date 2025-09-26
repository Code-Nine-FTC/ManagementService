package com.codenine.managementservice.utils.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.codenine.managementservice.dto.order.OrderRequest;
import com.codenine.managementservice.dto.order.OrderStatus;
import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.entity.Order;
import com.codenine.managementservice.entity.OrderItem;
import com.codenine.managementservice.entity.User;

public class OrderMapper {

  public static Order toEntity(OrderRequest orderRequest, User lastUser, List<Item> items) {
    Map<Long, Item> itemMap =
        items.stream().collect(Collectors.toMap(Item::getId, Function.identity()));
    Order order = new Order();
    order.setWithdrawDay(orderRequest.withdrawDay());
    order.setCreatedBy(lastUser);
    order.setLastUser(lastUser);
    order.setStatus(OrderStatus.PENDING.name());
    order.setCreatedAt(LocalDateTime.now());

    List<OrderItem> orderItems =
        orderRequest.itemQuantities().entrySet().stream()
            .map(
                entry -> {
                  Long itemId = entry.getKey();
                  Integer quantity = entry.getValue();
                  Item item = itemMap.get(itemId);
                  if (item == null) return null;
                  OrderItem orderItem = new OrderItem();
                  orderItem.setItem(item);
                  orderItem.setQuantity(quantity);
                  orderItem.setOrder(order);
                  orderItem.setLastUser(lastUser);
                  return orderItem;
                })
            .filter(oi -> oi != null)
            .collect(Collectors.toList());

    order.setOrderItems(orderItems);
    return order;
  }

  public static Order toUpdate(Order order, OrderRequest request, User lastUser, List<Item> items) {
    if (request.withdrawDay() != null) order.setWithdrawDay(request.withdrawDay());
    if (!request.itemQuantities().isEmpty()) {
      Map<Long, Item> itemMap =
          items.stream().collect(Collectors.toMap(Item::getId, Function.identity()));
      List<OrderItem> orderItems =
          request.itemQuantities().entrySet().stream()
              .map(
                  entry -> {
                    Long itemId = entry.getKey();
                    Integer quantity = entry.getValue();
                    Item item = itemMap.get(itemId);
                    if (item == null) return null;
                    OrderItem orderItem = new OrderItem();
                    orderItem.setItem(item);
                    orderItem.setQuantity(quantity);
                    orderItem.setOrder(order);
                    orderItem.setLastUser(lastUser);
                    return orderItem;
                  })
              .filter(oi -> oi != null)
              .collect(Collectors.toList());
      order.setOrderItems(orderItems);
    }
    if (!request.itemQuantities().isEmpty() || request.withdrawDay() != null) {
      order.setLastUser(lastUser);
      order.setLastUpdate(LocalDateTime.now());
    }
    return order;
  }
}
