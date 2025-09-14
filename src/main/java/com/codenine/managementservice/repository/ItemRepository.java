package com.codenine.managementservice.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.codenine.managementservice.dto.ItemResponse;
import com.codenine.managementservice.entity.Item;

public interface ItemRepository  extends JpaRepository<Item, Integer> {
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
            ti.id AS typeItemId,
            ti.name AS typeItemName,
            i.minimum_stock AS minimumStock,
            i.qr_code AS qrCode,
            u.name AS lastUserName,
            i.last_update AS lastUpdate
        FROM items i
        JOIN suppliers_companies sc ON i.supplier_id = sc.id
        JOIN type_items_items tii ON tii.items_id = i.id
        JOIN type_items ti ON ti.id = tii.type_items_id
        JOIN sections s ON s.id = ti.section_id
        JOIN users u ON i.last_user_id = u.id
        WHERE (:supplierId IS NULL OR sc.id = :supplierId)
        and (:sectionId IS NULL OR s.id = :sectionId)
        and (:typeItemId IS NULL OR ti.id = :typeItemId)
        and (:lastUserId IS NULL OR u.id = :lastUserId)
        and (:isActive IS NULL OR i.is_active = :isActive)
        and (:itemId IS NULL OR i.id = :itemId)
        """, nativeQuery = true)
        List<ItemResponse> findAllItemResponses(
            Integer supplierId,
            Integer sectionId,
            Integer typeItemId,
            Integer lastUserId,
            Boolean isActive,
            Integer itemId
        );

        

}
