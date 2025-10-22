package com.codenine.managementservice.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codenine.managementservice.dto.analytics.GroupDemandResponse;
import com.codenine.managementservice.dto.analytics.TopMaterialResponse;
import com.codenine.managementservice.dto.analytics.SectionConsumptionResponse;
import com.codenine.managementservice.entity.OrderItem;
import com.codenine.managementservice.entity.SectionType;

public interface AnalyticsRepository extends JpaRepository<OrderItem, Long> {

  @Query(
      """
      SELECT new com.codenine.managementservice.dto.analytics.TopMaterialResponse(
        i.id,
        i.name,
        it.id,
        it.name,
        COUNT(oi.id),
        SUM(oi.quantity)
      )
      FROM OrderItem oi
      JOIN oi.item i
      LEFT JOIN i.itemType it
      JOIN oi.order o
      WHERE (COALESCE(o.withdrawDay, o.createdAt) BETWEEN :startDate AND :endDate)
        AND (:onlyCompleted = false OR o.status = 'COMPLETED')
      GROUP BY i.id, i.name, it.id, it.name
      ORDER BY COUNT(oi.id) DESC, SUM(oi.quantity) DESC
      """)
  List<TopMaterialResponse> findTopMaterials(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("onlyCompleted") boolean onlyCompleted,
      Pageable pageable);

  @Query(
      """
      SELECT new com.codenine.managementservice.dto.analytics.GroupDemandResponse(
        it.id,
        it.name,
        COUNT(oi.id),
        SUM(oi.quantity)
      )
      FROM OrderItem oi
      JOIN oi.item i
      JOIN i.itemType it
      JOIN oi.order o
      WHERE (COALESCE(o.withdrawDay, o.createdAt) BETWEEN :startDate AND :endDate)
        AND (:onlyCompleted = false OR o.status = 'COMPLETED')
      GROUP BY it.id, it.name
      ORDER BY COUNT(oi.id) DESC, SUM(oi.quantity) DESC
      """)
  List<GroupDemandResponse> findGroupDemand(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("onlyCompleted") boolean onlyCompleted);

  @Query(
      """
      SELECT new com.codenine.managementservice.dto.analytics.SectionConsumptionResponse(
        s.id,
        s.title,
        COUNT(DISTINCT o.id),
        COALESCE(SUM(oi.quantity), 0)
      )
      FROM Order o
      LEFT JOIN o.orderItems oi
      JOIN o.section s
      WHERE (COALESCE(o.withdrawDay, o.createdAt) BETWEEN :startDate AND :endDate)
        AND (:onlyCompleted = false OR o.status = 'COMPLETED')
        AND (:type IS NULL OR s.sectionType = :type)
      GROUP BY s.id, s.title
      ORDER BY COUNT(DISTINCT o.id) DESC, SUM(oi.quantity) DESC
      """)
  List<SectionConsumptionResponse> findSectionConsumption(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("onlyCompleted") boolean onlyCompleted,
      @Param("type") SectionType type);

  @Query(
      value =
          """
          SELECT
            it.id as group_id,
            it.name as group_name,
            date_trunc(:step, COALESCE(o.withdraw_day, o.created_at)) as bucket,
            COUNT(oi.id) as pedidos,
            SUM(oi.quantity) as quantidade
          FROM order_item oi
          JOIN orders o ON oi.order_id = o.id
          JOIN items i ON oi.item_id = i.id
          JOIN items_type it ON i.id = ANY (SELECT item_id FROM item_item_type WHERE item_id = i.id) OR i.item_code IS NOT NULL
          LEFT JOIN item_item_type iit ON iit.item_id = i.id
          LEFT JOIN items_type it2 ON iit.item_type_id = it2.id
          WHERE (COALESCE(o.withdraw_day, o.created_at) BETWEEN :startDate AND :endDate)
            AND (:onlyCompleted = false OR o.status = 'COMPLETED')
          GROUP BY it.id, it.name, bucket
          ORDER BY bucket ASC, it.name ASC
          """,
      nativeQuery = true)
  List<Object[]> findGroupDemandSeries(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("onlyCompleted") boolean onlyCompleted,
      @Param("step") String step);

  @Query(
      value =
          """
          SELECT
            s.id as section_id,
            s.title as section_name,
            date_trunc(:step, COALESCE(o.withdraw_day, o.created_at)) as bucket,
            COUNT(DISTINCT o.id) as pedidos,
            COALESCE(SUM(oi.quantity), 0) as quantidade
          FROM orders o
          JOIN sections s ON o.section_id = s.id
          LEFT JOIN order_item oi ON oi.order_id = o.id
          WHERE (COALESCE(o.withdraw_day, o.created_at) BETWEEN :startDate AND :endDate)
            AND (:onlyCompleted = false OR o.status = 'COMPLETED')
            AND (:onlyConsumers = false OR s.section_type = 'CONSUMER')
          GROUP BY s.id, s.title, bucket
          ORDER BY bucket ASC, s.title ASC
          """,
      nativeQuery = true)
  List<Object[]> findSectionDemandSeries(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("onlyCompleted") boolean onlyCompleted,
      @Param("step") String step,
      @Param("onlyConsumers") boolean onlyConsumers);
}
