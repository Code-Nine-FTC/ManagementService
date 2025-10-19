package com.codenine.managementservice.repository;

import com.codenine.managementservice.dto.purchaseOrder.PurchaseOrderResponse;
import com.codenine.managementservice.entity.PurchaseOrder;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @Query(
        """
        SELECT new com.codenine.managementservice.dto.purchaseOrder.PurchaseOrderResponse(
            po.id as purchaseOrderId,
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
            o.id as orderId,
            o.status as orderStatus,
            sc.id as supplierCompanyId,
            sc.name as supplierCompanyName,
            sc.email as supplierCompanyEmail,
            lu.id as lastUserId,
            lu.name as lastUserName,
            cb.id as createdById,
            cb.name as createdByName
        )
        FROM PurchaseOrder po
        JOIN FETCH po.order o
        JOIN FETCH po.supplierCompany sc
        JOIN FETCH po.lastUser lu
        JOIN FETCH po.createdBy cb
        where 1=1
        and (:supplierCompanyId is null or sc.id = :supplierCompanyId)
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
}
