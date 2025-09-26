package com.codenine.managementservice.dto.order;

import java.time.LocalDateTime;

import org.springframework.boot.autoconfigure.security.SecurityProperties.User;

public record OrderResponse(
    Long id,
    LocalDateTime withdrawDay,
    String status,
    User createdBy,
    User lastUser,
    LocalDateTime createdAt) {}
