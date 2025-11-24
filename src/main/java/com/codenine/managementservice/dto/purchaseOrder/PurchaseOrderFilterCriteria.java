package com.codenine.managementservice.dto.purchaseOrder;

import java.time.LocalDateTime;

public record PurchaseOrderFilterCriteria(
    Long supplierCompanyId,
    String status,
    String emailStatus,
    LocalDateTime createdAfter,
    LocalDateTime createdBefore,
    Integer year) {}
