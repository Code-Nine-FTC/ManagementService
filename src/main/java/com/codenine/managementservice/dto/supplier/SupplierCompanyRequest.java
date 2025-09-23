package com.codenine.managementservice.dto.supplier;

import java.time.LocalDateTime;
import java.util.List;

public record SupplierCompanyRequest(
    String name,
    String email,
    String phoneNumber,
    String cnpj,
    Integer rating,
    Boolean isActive,
    Long lastUserId,
    List<Long> itemIds,
    List<Long> orderIds
) {}
