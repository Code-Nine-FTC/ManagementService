package com.codenine.managementservice.dto.user;

import java.util.List;

public record UserRequest(
    String name,
    String email,
    String password,
    Role role,
    List<Long> sectionIds
) {
}
