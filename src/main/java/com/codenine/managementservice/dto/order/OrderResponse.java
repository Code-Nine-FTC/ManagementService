package com.codenine.managementservice.dto.order;

import java.time.LocalDateTime;

import com.codenine.managementservice.entity.User;

public record OrderResponse(
        Long id,
        LocalDateTime withdrawDay,
        String status,
        User createdBy,
        User lastUser,
        LocalDateTime createdAt) {
}
