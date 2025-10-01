package com.codenine.managementservice.dto.supplier;

public record SupplierCompanyRequest(
                Long id,
                String name,
                String email,
                String phoneNumber,
                String cnpj,
                Integer rating,
                Boolean isActive,
                Long lastUserId) {
}
