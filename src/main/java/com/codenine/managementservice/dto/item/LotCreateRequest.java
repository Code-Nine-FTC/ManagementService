package com.codenine.managementservice.dto.item;

import java.time.LocalDate;

public record LotCreateRequest(Long itemId, String code, LocalDate expireDate, Integer quantity) {}
