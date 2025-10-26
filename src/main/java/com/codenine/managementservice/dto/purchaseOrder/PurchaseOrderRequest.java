package com.codenine.managementservice.dto.purchaseOrder;

import java.time.LocalDateTime;

public record PurchaseOrderRequest(
    String issuingBody,
    String commitmentNoteNumber,
    Integer year,
    String processNumber,
    Float totalValue,
    LocalDateTime issueDate,
    Long orderId,
    Long supplierCompanyId) {}
