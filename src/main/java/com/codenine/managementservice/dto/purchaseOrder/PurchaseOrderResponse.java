package com.codenine.managementservice.dto.purchaseOrder;

import java.time.LocalDateTime;

public record PurchaseOrderResponse(
    Long id,
    String issuingBody,
    String commitmentNoteNumber,
    Integer year,
    String processNumber,
    Float totalValue,
    LocalDateTime issueDate,
    Status status,
    EmailStatus emailStatus,
    LocalDateTime createdAt,
    LocalDateTime lastUpdate,
    Long orderId,
    String orderStatus,
    Long supplierCompanyId,
    String supplierCompanyName,
    String supplierCompanyEmail,
    Long lastUserId,
    String lastUserName,
    Long createdById,
    String createdByName
) {}