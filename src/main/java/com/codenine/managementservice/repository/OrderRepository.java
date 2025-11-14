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
      SELECT distinct new com.codenine.managementservice.dto.order.OrderResponse(
        o.id,
        o.orderNumber,
        o.withdrawDay,
        o.status,
        cb.id,
        cb.name,
        lu.id,
        lu.name,
        o.createdAt,
        o.lastUpdate,
        sec.id,
        sec.title
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
      @Param("sectionId") Long sectionId);

  @Query(
      """
      SELECT distinct new com.codenine.managementservice.dto.order.OrderResponse(
        o.id,
        o.orderNumber,
        o.withdrawDay,
        o.status,
        cb.id,
        cb.name,
        lu.id,
        lu.name,
        o.createdAt,
        o.lastUpdate,
        sec.id,
        sec.title
      )
      FROM Order o
      LEFT JOIN o.createdBy cb
      LEFT JOIN o.lastUser lu
      LEFT JOIN o.section sec
      WHERE (:sectionId IS NULL OR sec.id = :sectionId)
        AND o.status IN (:statuses)
      """)
  List<OrderResponse> findAllOrderResponsesByStatuses(
      @Param("statuses") List<String> statuses, @Param("sectionId") Long sectionId);

  interface OrderStatusCount {
    String getStatus();

    Long getTotal();
  }

  @Query(
      value =
          """
      WITH counts AS (
        SELECT sec.id    AS section_id,
               sec.title AS section_name,
               SUM(CASE WHEN o.status = 'PENDING'    THEN 1 ELSE 0 END) AS pending,
               SUM(CASE WHEN o.status = 'APPROVED'   THEN 1 ELSE 0 END) AS approved,
               SUM(CASE WHEN o.status = 'PROCESSING' THEN 1 ELSE 0 END) AS processing,
               SUM(CASE WHEN o.status = 'COMPLETED'  THEN 1 ELSE 0 END) AS completed,
               SUM(CASE WHEN o.status = 'CANCELLED'  THEN 1 ELSE 0 END) AS cancelled,
               COUNT(*)                                         AS total
        FROM orders o
        LEFT JOIN sections sec ON o.section_id = sec.id
        GROUP BY sec.id, sec.title
      )
      SELECT section_id, section_name, pending, approved, processing, completed, cancelled, total
      FROM counts
      ORDER BY section_name
      """,
      nativeQuery = true)
  List<Object[]> sectionOrderStatusCountsNative();

  @Query(
      """
      select new com.codenine.managementservice.dto.order.OrderItemResponse(
          oi.id,
          o.id as ordemId,
          oi.item.id,
          oi.item.name,
          oi.quantity
      )
      from Order o
      join o.orderItems oi
      where o.id = :orderId
        """)
  List<OrderItemResponse> findAllOrderItemResponsesByOrderId(@Param("orderId") Long orderId);

  boolean existsByOrderNumber(String orderNumber);
}
