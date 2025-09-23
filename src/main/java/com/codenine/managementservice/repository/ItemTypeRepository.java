package com.codenine.managementservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codenine.managementservice.dto.itemType.ItemTypeResponse;
import com.codenine.managementservice.entity.ItemType;

public interface ItemTypeRepository extends JpaRepository<ItemType, Long> {

  @Query(
      """
            Select new com.codenine.managementservice.dto.itemType.ItemTypeResponse(
            it.id,
            it.name,
            s.id,
            s.title as sectionName,
            u.id,
            u.name,
            it.lastUpdate
            )
            from ItemType it
            join it.section s
            left join it.lastUser u
            where (:itemTypeId IS NULL OR it.id = :itemTypeId)
            and (:sectionId IS NULL OR s.id = :sectionId)
            and (:lastUserId IS NULL OR u.id = :lastUserId)
            """)
  List<ItemTypeResponse> findAllItemTypeResponses(
      @Param("itemTypeId") Long itemTypeId,
      @Param("sectionId") Long sectionId,
      @Param("lastUserId") Long lastUserId);
}
