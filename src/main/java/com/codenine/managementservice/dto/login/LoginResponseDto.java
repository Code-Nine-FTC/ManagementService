package com.codenine.managementservice.dto.login;

import java.util.List;

import com.codenine.managementservice.dto.section.SectionDto;

public record LoginResponseDto(String token, String name, String email, String role, List<SectionDto> sections) {}
