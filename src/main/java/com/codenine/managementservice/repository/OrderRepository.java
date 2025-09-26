package com.codenine.managementservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codenine.managementservice.dto.order.OrderItemResponse;
import com.codenine.managementservice.dto.order.OrderResponse;
import com.codenine.managementservice.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

  @Query(
      """
      SELECT new com.codenine.managementservice.dto.order.OrderResponse(
          o.id,
          o.withdrawDay,
          o.status,
          o.createdBy,
          o.lastUser,
          o.createdAt
      )
      FROM Order o
      LEFT JOIN o.createdBy cb
      LEFT JOIN o.lastUser lu
      WHERE (:orderId IS NULL OR o.id = :orderId)
        AND (:status IS NULL OR o.status = :status)
      """)
  List<OrderResponse> findAllOrderResponses(
      @Param("orderId") Long orderId, @Param("status") String status);

  @Query(
      """
      select new com.codenine.managementservice.dto.order.OrderItemResponse(
          o.id,
          oi.item.id,
          oi.item.name,
          oi.quantity,
          oi.item.supplier.id,
          oi.item.supplier.name
      )
      from Order o
      join o.orderItems oi
      where o.id = :orderId
        """)
  List<OrderItemResponse> findAllOrderItemResponsesByOrderId(@Param("orderId") Long orderId);
}
