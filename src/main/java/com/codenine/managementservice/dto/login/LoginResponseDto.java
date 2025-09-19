package com.codenine.managementservice.dto.login;

import java.util.List;

public record LoginResponseDto(
        String token,
        String email,
        String role,
        List<Long> sectionIds
) {
}
