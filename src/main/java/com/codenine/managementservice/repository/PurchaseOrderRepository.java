package com.codenine.managementservice.repository;

import com.codenine.managementservice.dto.purchaseOrder.PurchaseOrderResponse;
import com.codenine.managementservice.entity.PurchaseOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @Query(
        """
        SELECT new com.codenine.managementservice.dto.purchaseOrder.PurchaseOrderResponse(
            po.id,
            po.issuingBody,
            po.commitmentNoteNumber,
            po.year,
            po.processNumber,
            po.totalValue,
            po.issueDate,
            po.status,
            po.emailStatus,
            po.createdAt,
            po.lastUpdate,
            o.id,
            o.status,
            sc.id,
            sc.name,
            sc.email,
            lu.id,
            lu.name,
            cb.id,
            cb.name
        )
        FROM PurchaseOrder po
        JOIN po.order o
        JOIN po.supplierCompany sc
        JOIN po.lastUser lu
        JOIN po.createdBy cb
        where (:supplierCompanyId is null or sc.id = :supplierCompanyId)
        and (:orderId is null or o.id = :orderId)
        and (:status is null or po.status = :status)
        and (:emailStatus is null or po.emailStatus = :emailStatus)
        and (:createdAfter is null or po.createdAt >= :createdAfter)
        and (:createdBefore is null or po.createdAt <= :createdBefore)
        and (:year is null or po.year = :year)
        """
    )
    List<PurchaseOrderResponse> findAllPurchaseOrders(
        @Param("supplierCompanyId") Long supplierCompanyId,
        @Param("orderId") Long orderId,
        @Param("status") String status,
        @Param("emailStatus") String emailStatus,
        @Param("createdAfter") LocalDateTime createdAfter,
        @Param("createdBefore") LocalDateTime createdBefore,
        @Param("year") Integer year
    );

    @Query("SELECT po FROM PurchaseOrder po WHERE po.status = 'PENDING' AND po.createdAt < :date")
    List<PurchaseOrder> findPendingOrdersOlderThan(@Param("date") LocalDate date);
}
