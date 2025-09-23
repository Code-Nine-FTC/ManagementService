package com.codenine.managementservice.dto.user;

import java.util.List;

public record UserResponse(
    Long id,
    String username,
    String email,
    Role role,
    boolean isActive,
    List<Long> sectionIds
) {
}
