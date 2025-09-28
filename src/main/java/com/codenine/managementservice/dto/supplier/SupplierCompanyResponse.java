package com.codenine.managementservice.dto.supplier;

import java.time.LocalDateTime;
import java.util.List;

public record SupplierCompanyResponse(
    Long id,
    String name,
    String email,
    String phoneNumber,
    String cnpj,
    Boolean isActive,
    Integer rating,
    LocalDateTime lastUpdate,
    String lastUserName,
    List<Long> itemIds) {}
