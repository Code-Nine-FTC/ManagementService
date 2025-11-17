package com.codenine.managementservice.dto.order;

public record SectionOrderStatusCount(
    Long sectionId,
    String sectionName,
    Long pending,
    Long approved,
    Long processing,
    Long completed,
    Long cancelled,
    Long total) {}
