package com.codenine.managementservice.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codenine.managementservice.dto.item.ItemResponse;
import com.codenine.managementservice.entity.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
  @Query(
      """
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
              i.itemCode,
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
            and (:itemCode IS NULL OR i.itemCode = :itemCode)
      """)
  List<ItemResponse> findAllItemResponses(
      @Param("itemCode") String itemCode,
      @Param("sectionId") Long sectionId,
      @Param("itemTypeId") Long itemTypeId,
      @Param("isActive") Boolean isActive,
      @Param("itemId") Long itemId);

  // Métodos genéricos para itens expirados por seção
  @Query(
      """
      SELECT COUNT(i)
      FROM Item i
      JOIN i.itemType it
      JOIN it.section s
      WHERE s.id IN :sectionIds
        AND LOWER(s.title) = :sectionName
        AND i.currentStock > 0
        AND i.expireDate IS NOT NULL
        AND i.expireDate < :now
        AND i.isActive = true
      """)
  long countExpiredItemsBySection(
      @Param("sectionIds") List<Long> sectionIds,
      @Param("sectionName") String sectionName,
      @Param("now") LocalDateTime now);

  @Query(
      """
      SELECT COUNT(i)
      FROM Item i
      JOIN i.itemType it
      JOIN it.section s
      WHERE s.id IN :sectionIds
        AND LOWER(s.title) = :sectionName
        AND i.currentStock > 0
        AND i.expireDate IS NOT NULL
        AND i.expireDate >= :now
        AND i.expireDate <= :futureDate
        AND i.isActive = true
      """)
  long countExpiringSoonItemsBySection(
      @Param("sectionIds") List<Long> sectionIds,
      @Param("sectionName") String sectionName,
      @Param("now") LocalDateTime now,
      @Param("futureDate") LocalDateTime futureDate);

  @Query(
      """
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
          i.itemCode,
          u.name,
          i.lastUpdate
      )
      FROM Item i
      JOIN i.itemType it
      JOIN it.section s
      JOIN i.lastUser u
      WHERE s.id IN :sectionIds
        AND LOWER(s.title) = :sectionName
        AND i.currentStock > 0
        AND i.expireDate IS NOT NULL
        AND i.expireDate < :now
        AND i.isActive = true
      ORDER BY i.expireDate ASC
      """)
  List<ItemResponse> findExpiredItemsBySection(
      @Param("sectionIds") List<Long> sectionIds,
      @Param("sectionName") String sectionName,
      @Param("now") LocalDateTime now);

  @Query(
      """
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
          i.itemCode,
          u.name,
          i.lastUpdate
      )
      FROM Item i
      JOIN i.itemType it
      JOIN it.section s
      JOIN i.lastUser u
      WHERE s.id IN :sectionIds
        AND LOWER(s.title) = :sectionName
        AND i.currentStock > 0
        AND i.expireDate IS NOT NULL
        AND i.expireDate >= :now
        AND i.expireDate <= :futureDate
        AND i.isActive = true
      ORDER BY i.expireDate ASC
      """)
  List<ItemResponse> findExpiringSoonItemsBySection(
      @Param("sectionIds") List<Long> sectionIds,
      @Param("sectionName") String sectionName,
      @Param("now") LocalDateTime now,
      @Param("futureDate") LocalDateTime futureDate);

  // Métodos para buscar todos os itens (independente da seção)
  @Query(
      """
      SELECT COUNT(i)
      FROM Item i
      JOIN i.itemType it
      JOIN it.section s
      WHERE s.id IN :sectionIds
        AND i.currentStock > 0
        AND i.expireDate IS NOT NULL
        AND i.expireDate < :now
        AND i.isActive = true
      """)
  long countAllExpiredItemsByUserSections(
      @Param("sectionIds") List<Long> sectionIds, @Param("now") LocalDateTime now);

  @Query(
      """
      SELECT COUNT(i)
      FROM Item i
      JOIN i.itemType it
      JOIN it.section s
      WHERE s.id IN :sectionIds
        AND i.currentStock > 0
        AND i.expireDate IS NOT NULL
        AND i.expireDate >= :now
        AND i.expireDate <= :futureDate
        AND i.isActive = true
      """)
  long countAllExpiringSoonItemsByUserSections(
      @Param("sectionIds") List<Long> sectionIds,
      @Param("now") LocalDateTime now,
      @Param("futureDate") LocalDateTime futureDate);

  @Query(
      """
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
          i.itemCode,
          u.name,
          i.lastUpdate
      )
      FROM Item i
      JOIN i.itemType it
      JOIN it.section s
      JOIN i.lastUser u
      WHERE s.id IN :sectionIds
        AND i.currentStock > 0
        AND i.expireDate IS NOT NULL
        AND i.expireDate < :now
        AND i.isActive = true
      ORDER BY i.expireDate ASC
      """)
  List<ItemResponse> findAllExpiredItemsByUserSections(
      @Param("sectionIds") List<Long> sectionIds, @Param("now") LocalDateTime now);

  @Query(
      """
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
          i.itemCode,
          u.name,
          i.lastUpdate
      )
      FROM Item i
      JOIN i.itemType it
      JOIN it.section s
      JOIN i.lastUser u
      WHERE s.id IN :sectionIds
        AND i.currentStock > 0
        AND i.expireDate IS NOT NULL
        AND i.expireDate >= :now
        AND i.expireDate <= :futureDate
        AND i.isActive = true
      ORDER BY i.expireDate ASC
      """)
  List<ItemResponse> findAllExpiringSoonItemsByUserSections(
      @Param("sectionIds") List<Long> sectionIds,
      @Param("now") LocalDateTime now,
      @Param("futureDate") LocalDateTime futureDate);
}
