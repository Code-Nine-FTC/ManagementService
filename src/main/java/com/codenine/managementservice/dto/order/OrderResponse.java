package com.codenine.managementservice.dto.order;

import java.time.LocalDateTime;
import java.util.List;

import com.codenine.managementservice.dto.item.ItemResponse;
import com.codenine.managementservice.dto.supplier.SupplierCompanyResponse;
import com.codenine.managementservice.dto.user.UserRequest;

public record OrderResponse(
    Long id,
    String status,
    LocalDateTime withdrawDay,
    LocalDateTime createdAt,
    UserRequest createdBy,
    List<ItemResponse> items,
    List<SupplierCompanyResponse> suppliers) {}
