package com.codenine.managementservice.utils.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;

import com.codenine.managementservice.dto.order.OrderRequest;
import com.codenine.managementservice.dto.order.OrderStatus;
import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.entity.Order;
import com.codenine.managementservice.entity.OrderItem;
import com.codenine.managementservice.entity.User;

public class OrderMapper {

    public static Object[] toEntity(OrderRequest orderRequest, User lastUser, OrderItem orderItem, List<Item> items) {
        Map<Long, Item> itemMap = items.stream().collect(Collectors.toMap(Item::getId, Function.identity()));
        Order order = new Order();
        order.setWithdrawDay(orderRequest.withdrawDay());
        order.setCreatedBy(lastUser);
        order.setLastUser(lastUser);
        order.setStatus(OrderStatus.PENDING.name());
        order.setCreatedAt(LocalDateTime.now());
        orderItem.setLastUser(lastUser);
        for (Map.Entry<Long, Integer> entry : orderRequest.itemQuantities().entrySet()) {
            Long itemId = entry.getKey();
            Integer quantity = entry.getValue();
            Item item = itemMap.get(itemId);
            if (item == null)
                continue;
            orderItem.setItem(item);
            orderItem.setQuantity(quantity);
            orderItem.setOrder(order);
            orderItem.setLastUser(lastUser);
            order.getOrderItems().add(orderItem);
        }

        return new Object[] { order, orderItem };
    }

    public static Order toUpdate(Order order, OrderRequest request) {
        return order;
    }
}
