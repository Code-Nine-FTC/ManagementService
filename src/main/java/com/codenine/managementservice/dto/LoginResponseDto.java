package com.codenine.managementservice.dto;

public record LoginResponseDto(
        String token,
        String email,
        String role,
        Long sectionId,
        String sectionName
) {
}
