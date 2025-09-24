package com.codenine.managementservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codenine.managementservice.dto.itemLoss.ItemLossResponse;
import com.codenine.managementservice.entity.ItemLoss;

public interface ItemLossRepository extends JpaRepository<ItemLoss, Long> {
  
  @Query("""
      SELECT new com.codenine.managementservice.dto.itemLoss.ItemLossResponse(
          il.id,
          il.reason,
          il.lostQuantity,
          il.createDate,
          il.lastUpdate,
          new com.codenine.managementservice.dto.user.UserRequest(
              rb.name,
              rb.email,
              null,
              rb.role,
              null
          ),
          new com.codenine.managementservice.dto.item.ItemResponse(
              i.id,
              i.name,
              i.currentStock,
              i.measure,
              i.expireDate,
              sc.id,
              sc.name,
              s.id,
              s.title,
              it.id,
              it.name,
              i.minimumStock,
              i.qrCode,
              iu.name,
              i.lastUpdate
          ),
          lu.name
      )
      FROM ItemLoss il
      JOIN il.item i
      JOIN i.supplier sc
      JOIN i.itemType it
      JOIN it.section s
      JOIN i.lastUser iu
      JOIN il.recordedBy rb
      LEFT JOIN il.lastUser lu
      WHERE (:itemLossId IS NULL OR il.id = :itemLossId)
        AND (:itemId IS NULL OR i.id = :itemId)
        AND (:recordedById IS NULL OR rb.id = :recordedById)
        AND (:lastUserId IS NULL OR lu.id = :lastUserId)
        AND (:sectionId IS NULL OR s.id = :sectionId)
      ORDER BY il.createDate DESC
      """)
  List<ItemLossResponse> findAllItemLossResponses(
      @Param("itemLossId") Long itemLossId,
      @Param("itemId") Long itemId,
      @Param("recordedById") Long recordedById,
      @Param("lastUserId") Long lastUserId,
      @Param("sectionId") Long sectionId);
}
