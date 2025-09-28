package com.codenine.managementservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codenine.managementservice.dto.section.SectionResponse;
import com.codenine.managementservice.entity.Section;

public interface SectionRepository extends JpaRepository<Section, Long> {

  @Query(
      """
            Select new com.codenine.managementservice.dto.section.SectionResponse(
            s.id,
            s.title,
            s.roleAccess,
            s.isActive,
            s.createdAt,
            s.lastUpdate,
            u.name
            )
            from Section s
            left join s.lastUser u
            where (:sectionId IS NULL OR s.id = :sectionId)
            and (:lastUserId IS NULL OR u.id = :lastUserId)
            and (:roleAccess IS NULL OR s.roleAccess = :roleAccess)
            and (:isActive IS NULL OR s.isActive = :isActive)
            """)
  List<SectionResponse> findAllSectionResponses(
      @Param("sectionId") Long sectionId,
      @Param("lastUserId") Long lastUserId,
      @Param("roleAccess") Integer roleAccess,
      @Param("isActive") Boolean isActive);
}
