package com.codenine.managementservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codenine.managementservice.dto.item.ItemResponse;
import com.codenine.managementservice.entity.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
  @Query("""
          SELECT new com.codenine.managementservice.dto.item.ItemResponse(
              i.id,
              i.name,
              i.currentStock,
              i.measure,
              i.expireDate,
              s.id,
              s.title,
              it.id,
              it.name,
              i.minimumStock,
              i.qrCode,
              u.name,
              i.lastUpdate
          )
          FROM Item i
          JOIN i.itemType it
          JOIN it.section s
          JOIN i.lastUser u
          WHERE (:sectionId IS NULL OR s.id = :sectionId)
            AND (:itemTypeId IS NULL OR it.id = :itemTypeId)
            AND (:isActive IS NULL OR i.isActive = :isActive)
            AND (:itemId IS NULL OR i.id = :itemId)
      """)
  List<ItemResponse> findAllItemResponses(
      @Param("sectionId") Long sectionId,
      @Param("itemTypeId") Long itemTypeId,
      @Param("isActive") Boolean isActive,
      @Param("itemId") Long itemId);
}
