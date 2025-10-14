package com.codenine.managementservice.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codenine.managementservice.dto.itemLoss.ItemLossResponse;
import com.codenine.managementservice.entity.ItemLoss;

public interface ItemLossRepository extends JpaRepository<ItemLoss, Long> {
  @Query(
      """
        select new com.codenine.managementservice.dto.itemLoss.ItemLossResponse(
            il.id,
            il.reason,
            il.lostQuantity,
            il.createDate,
            il.lastUpdate,
            i.id,
            i.name,
            rb.id,
            rb.name,
            lu.id,
            lu.name
        )
        from ItemLoss il
        join il.item i
        join il.recordedBy rb
        join il.lastUser lu
        where (
            (:itemId is null or il.item.id = :itemId) and
            (:recordedById is null or il.recordedBy.id = :recordedById) and
            (:startDate is null or il.createDate >= :startDate) and
            (:endDate is null or il.createDate <= :endDate) and
            (:itemLossId is null or il.id = :itemLossId)
        )
    """)
  List<ItemLossResponse> findAllByFilter(
      @Param("itemId") Long itemId,
      @Param("recordedById") Long recordedById,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("itemLossId") Long itemLossId);
}
