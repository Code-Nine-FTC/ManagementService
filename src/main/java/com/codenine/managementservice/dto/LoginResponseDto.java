package com.codenine.managementservice.dto;

import java.util.List;

public record LoginResponseDto(
        String token,
        String email,
        String role,
        List<Long> sectionIds
) {
}
