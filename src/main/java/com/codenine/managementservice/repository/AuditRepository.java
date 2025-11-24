package com.codenine.managementservice.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codenine.managementservice.dto.action.ActionType;
import com.codenine.managementservice.entity.AuditLog;

public interface AuditRepository extends JpaRepository<AuditLog, Long> {

  @Query(
      """
      SELECT a FROM AuditLog a LEFT JOIN a.user u
      WHERE (:userId IS NULL OR u.id = :userId)
      AND (:actionType IS NULL OR a.actionType = :actionType)
      AND (CAST(:startDate AS timestamp) IS NULL OR a.createdAt >= :startDate)
      AND (CAST(:endDate AS timestamp) IS NULL OR a.createdAt <= :endDate)
      ORDER BY a.createdAt DESC
      """)
  Page<AuditLog> findAllWithFilters(
      @Param("userId") Long userId,
      @Param("actionType") ActionType actionType,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      Pageable pageable);
}
