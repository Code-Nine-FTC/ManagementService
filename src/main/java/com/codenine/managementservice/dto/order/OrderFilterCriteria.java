package com.codenine.managementservice.dto.order;

public record OrderFilterCriteria(Long userId, OrderStatus status, Long supplierId, Long sectionId) {
}
