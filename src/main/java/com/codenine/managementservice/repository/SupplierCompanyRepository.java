package com.codenine.managementservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codenine.managementservice.dto.supplier.SupplierCompanyResponse;
import com.codenine.managementservice.entity.SupplierCompany;

public interface SupplierCompanyRepository extends JpaRepository<SupplierCompany, Long> {

  @Query(
      """
            SELECT new com.codenine.managementservice.dto.supplier.SupplierCompanyResponse(
                sc.id,
                sc.name,
                sc.email,
                sc.phoneNumber,
                sc.cnpj,
                sc.isActive,
                sc.rating,
                sc.lastUpdate,
                u.name,
                null,
                null
            )
            FROM SupplierCompany sc
            LEFT JOIN sc.lastUser u
            WHERE (:supplierId IS NULL OR sc.id = :supplierId)
              AND (:isActive IS NULL OR sc.isActive = :isActive)
              AND (:lastUserId IS NULL OR u.id = :lastUserId)
            """)
  List<SupplierCompanyResponse> findAllSupplierCompanyResponses(
      @Param("supplierId") Long supplierId,
      @Param("isActive") Boolean isActive,
      @Param("lastUserId") Long lastUserId);
}
