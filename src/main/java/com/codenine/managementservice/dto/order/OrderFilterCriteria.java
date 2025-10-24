package com.codenine.managementservice.dto.order;

public record OrderFilterCriteria(
    Long orderId, OrderStatus status, Long supplierId, Long sectionId) {}
