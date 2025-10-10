package com.codenine.managementservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codenine.managementservice.entity.OrderItem;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
		@Query("""
				SELECT s.id, s.title, SUM(oi.quantity) as totalQuantity, COUNT(DISTINCT o.id) as requestsCount
				FROM OrderItem oi
				JOIN oi.order o
				JOIN o.section s
				WHERE o.withdrawDay >= :from AND o.withdrawDay <= :to
					AND o.status IN :statuses
				GROUP BY s.id, s.title
		""")
		List<Object[]> findConsumptionSummary(@Param("from") LocalDateTime from,
																					@Param("to") LocalDateTime to,
																					@Param("statuses") List<String> statuses);

		@Query("""
				SELECT oi
				FROM OrderItem oi
				JOIN oi.order o
				WHERE o.withdrawDay >= :from AND o.withdrawDay <= :to
					AND o.status IN :statuses
		""")
		List<OrderItem> findByOrderWithdrawDayBetweenAndOrderStatusIn(@Param("from") LocalDateTime from,
																																	@Param("to") LocalDateTime to,
																																	@Param("statuses") List<String> statuses);
}
