package com.codenine.managementservice.dto.order;

import java.util.Map;

public record OrderRequest(Map<Long, Integer> itemQuantities, Long supplierId) {}
