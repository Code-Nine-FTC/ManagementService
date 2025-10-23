package com.codenine.managementservice.dto.item;

import java.time.LocalDate;

public record LotResponse(Long id, Long itemId, String itemName, String code, LocalDate expireDate, Integer quantityOnHand) {}
