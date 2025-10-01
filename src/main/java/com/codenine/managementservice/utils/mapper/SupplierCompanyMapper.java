package com.codenine.managementservice.utils.mapper;

import java.time.LocalDateTime;

import com.codenine.managementservice.dto.supplier.SupplierCompanyRequest;
import com.codenine.managementservice.entity.SupplierCompany;
import com.codenine.managementservice.entity.User;

public class SupplierCompanyMapper {

    public static SupplierCompany toEntity(SupplierCompanyRequest request, User lastUser) {
        SupplierCompany entity = new SupplierCompany();
        entity.setName(request.name());
        entity.setEmail(request.email());
        entity.setPhoneNumber(request.phoneNumber());
        entity.setCnpj(request.cnpj());
        entity.setIsActive(request.isActive());
        entity.setRating(request.rating());
        entity.setLastUser(lastUser);
        entity.setLastUpdate(LocalDateTime.now());
        return entity;
    }

    public static SupplierCompany toUpdate(SupplierCompany entity, SupplierCompanyRequest request, User lastUser) {
        if (request.name() != null) {
            entity.setName(request.name());
        }
        if (request.email() != null) {
            entity.setEmail(request.email());
        }
        if (request.phoneNumber() != null) {
            entity.setPhoneNumber(request.phoneNumber());
        }
        if (request.cnpj() != null) {
            entity.setCnpj(request.cnpj());
        }
        if (request.rating() != null) {
            entity.setRating(request.rating());
        }
        if (request.name() != null || request.email() != null || request.phoneNumber() != null || request.cnpj() != null
                || request.rating() != null) {
            entity.setLastUpdate(LocalDateTime.now());
            entity.setLastUser(lastUser);
        }
        return entity;
    }
}
