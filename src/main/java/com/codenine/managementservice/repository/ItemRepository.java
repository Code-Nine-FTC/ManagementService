package com.codenine.managementservice.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.codenine.managementservice.dto.ItemResponse;
import com.codenine.managementservice.entity.Item;

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
        JOIN items_type it ON i.item_type_id = it.id
        JOIN sections s ON it.section_id = s.id
        JOIN users u ON i.last_user_id = u.id
        WHERE (:supplierId IS NULL OR sc.id = :supplierId)
        and (:sectionId IS NULL OR s.id = :sectionId)
        and (:itemTypeId IS NULL OR it.id = :itemTypeId)
        and (:lastUserId IS NULL OR u.id = :lastUserId)
        and (:isActive IS NULL OR i.is_active = :isActive)
        and (:itemId IS NULL OR i.id = :itemId)
        """, nativeQuery = true)
        List<ItemResponse> findAllItemResponses(
            Long supplierId,
            Long sectionId,
            Long itemTypeId,
            Long lastUserId,
            Boolean isActive,
            Long itemId
        );

        

}
