package com.codenine.managementservice.repository;
import java.util.List;

import com.codenine.managementservice.dto.ItemResponseProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.codenine.managementservice.entity.Item;
import org.springframework.data.repository.query.Param;

public interface ItemRepository  extends JpaRepository<Item, Long> {
    @Query(value = """
        SELECT
           i.id AS itemId,
           i.name,
           i.current_stock AS currentStock,
           i.measure,
           i.expire_date AS expireDate,
           sc.id AS supplierId,
           sc.name AS supplierName,
           s.id AS sectionId,
           s.title AS sectionName,
           it.id AS itemTypeId,
           it.name AS itemTypeName,
           i.minimum_stock AS minimumStock,
           i.qr_code AS qrCode,
           u.name AS lastUserName,
           i.last_update AS lastUpdate
       FROM items i
       JOIN suppliers_companies sc ON i.supplier_id = sc.id
       JOIN item_item_type iit ON i.id = iit.item_id
       JOIN items_type it ON iit.item_type_id = it.id
       JOIN itemtype_section its ON it.id = its.item_type_id
       JOIN sections s ON its.section_id = s.id
       JOIN users u ON i.last_user_id = u.id
       WHERE (:supplierId IS NULL OR sc.id = :supplierId)
         AND (:sectionId IS NULL OR s.id = :sectionId)
         AND (:itemTypeId IS NULL OR it.id = :itemTypeId)
         AND (:lastUserId IS NULL OR u.id = :lastUserId)
         AND (:isActive IS NULL OR i.is_active = :isActive)
         AND (:itemId IS NULL OR i.id = :itemId)
    """, nativeQuery = true)
        List<ItemResponseProjection> findAllItemResponses(
                @Param("supplierId") Long supplierId,
                @Param("sectionId") Long sectionId,
                @Param("itemTypeId") Long itemTypeId,
                @Param("lastUserId") Long lastUserId,
                @Param("isActive") Boolean isActive,
                @Param("itemId") Long itemId
        );
}
