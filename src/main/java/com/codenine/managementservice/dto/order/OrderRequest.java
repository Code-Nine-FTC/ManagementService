package com.codenine.managementservice.dto.order;

import java.time.LocalDateTime;
import java.util.Map;

public record OrderRequest(LocalDateTime withdrawDay, Map<Long, Integer> itemQuantities) {
}
