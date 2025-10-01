package com.codenine.managementservice.dto.supplier;

import java.time.LocalDateTime;

public record SupplierCompanyResponse(
        Long id,
        String name,
        String email,
        String phoneNumber,
        String cnpj,
        Boolean isActive,
        Integer rating,
        LocalDateTime lastUpdate,
        Long lastUserId,
        String lastUserName) {
}
