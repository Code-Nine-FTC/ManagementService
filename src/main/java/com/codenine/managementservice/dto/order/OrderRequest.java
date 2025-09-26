package com.codenine.managementservice.dto.order;

import java.time.LocalDateTime;
import java.util.List;

public record OrderRequest(
    LocalDateTime withdrawDay, List<Long> itemIds, List<Long> supplierIds, String status) {}
