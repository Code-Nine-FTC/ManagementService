package com.codenine.managementservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codenine.managementservice.dto.order.OrderItemResponse;
import com.codenine.managementservice.dto.order.OrderResponse;
import com.codenine.managementservice.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

  @Query("""
      SELECT distinct new com.codenine.managementservice.dto.order.OrderResponse(
        o.id,
        o.withdrawDay,
        o.status,
        cb.id,
        cb.name,
        lu.id,
        lu.name,
        o.createdAt
      )
      FROM Order o
      LEFT JOIN o.createdBy cb
      LEFT JOIN o.lastUser lu
      LEFT JOIN o.section sec
      WHERE (:orderId IS NULL OR o.id = :orderId)
        AND (:status IS NULL OR o.status = :status)
        AND (:sectionId IS NULL OR sec.id = :sectionId)

      """)
  List<OrderResponse> findAllOrderResponses(
      @Param("orderId") Long orderId,
      @Param("status") String status,
      @Param("supplierId") Long supplierId,
      @Param("sectionId") Long sectionId);

  @Query("""
      select new com.codenine.managementservice.dto.order.OrderItemResponse(
          oi.id,
          o.id as ordemId,
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
