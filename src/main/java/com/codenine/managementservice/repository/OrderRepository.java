package com.codenine.managementservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codenine.managementservice.dto.order.OrderResponse;
import com.codenine.managementservice.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

  @Query(
      """
            SELECT new com.codenine.managementservice.dto.order.OrderResponse(
                o.id,
                o.status,
                o.withdrawDay,
                o.createdAt,
                null,
                null,
                null
            )
            FROM Order o
            LEFT JOIN o.createdBy cb
            LEFT JOIN o.lastUser lu
            WHERE (:orderId IS NULL OR o.id = :orderId)
              AND (:status IS NULL OR o.status = :status)
              AND (:createdById IS NULL OR cb.id = :createdById)
              AND (:lastUserId IS NULL OR lu.id = :lastUserId)
            """)
  List<OrderResponse> findAllOrderResponses(
      @Param("orderId") Long orderId,
      @Param("status") String status,
      @Param("createdById") Long createdById,
      @Param("lastUserId") Long lastUserId);
}