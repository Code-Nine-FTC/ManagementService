package com.codenine.managementservice.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codenine.managementservice.dto.item.ItemResponse;
import com.codenine.managementservice.entity.Item;

public interface PharmacyRepository extends JpaRepository<Item, Long> {

  // Contar itens vencidos da farmácia
  @Query(
      """
      SELECT COUNT(i)
      FROM Item i
      JOIN i.itemType it
      JOIN it.section s
      WHERE LOWER(s.title) = 'farmácia'
        AND s.id IN :sectionIds
        AND i.currentStock > 0
        AND i.expireDate IS NOT NULL
        AND i.expireDate < :now
        AND i.isActive = true
      """)
  long countExpiredItems(
      @Param("sectionIds") List<Long> sectionIds, @Param("now") LocalDateTime now);

  // Contar itens próximos do vencimento da farmácia
  @Query(
      """
      SELECT COUNT(i)
      FROM Item i
      JOIN i.itemType it
      JOIN it.section s
      WHERE LOWER(s.title) = 'farmácia'
        AND s.id IN :sectionIds
        AND i.currentStock > 0
        AND i.expireDate IS NOT NULL
        AND i.expireDate >= :now
        AND i.expireDate <= :futureDate
        AND i.isActive = true
      """)
  long countExpiringSoonItems(
      @Param("sectionIds") List<Long> sectionIds,
      @Param("now") LocalDateTime now,
      @Param("futureDate") LocalDateTime futureDate);

  // Buscar itens vencidos da farmácia
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
      WHERE LOWER(s.title) = 'farmácia'
        AND s.id IN :sectionIds
        AND i.currentStock > 0
        AND i.expireDate IS NOT NULL
        AND i.expireDate < :now
        AND i.isActive = true
      ORDER BY i.expireDate ASC
      """)
  List<ItemResponse> findExpiredItems(
      @Param("sectionIds") List<Long> sectionIds, @Param("now") LocalDateTime now);

  // Buscar itens próximos do vencimento da farmácia
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
      WHERE LOWER(s.title) = 'farmácia'
        AND s.id IN :sectionIds
        AND i.currentStock > 0
        AND i.expireDate IS NOT NULL
        AND i.expireDate >= :now
        AND i.expireDate <= :futureDate
        AND i.isActive = true
      ORDER BY i.expireDate ASC
      """)
  List<ItemResponse> findExpiringSoonItems(
      @Param("sectionIds") List<Long> sectionIds,
      @Param("now") LocalDateTime now,
      @Param("futureDate") LocalDateTime futureDate);
}
