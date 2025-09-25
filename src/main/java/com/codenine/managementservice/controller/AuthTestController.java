package com.codenine.managementservice.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codenine.managementservice.dto.section.SectionDto;
import com.codenine.managementservice.entity.User;

import io.swagger.v3.oas.annotations.Operation;

@RestController
public class AuthTestController {

  /**
   * Retorna informações do usuário autenticado (email, nome, papel e seções).
   *
   * @return Dados do usuário autenticado.
   */
  @Operation(
      description = "Retorna informações do usuário autenticado (email, nome, papel e seções).")
  @GetMapping("/auth/me")
  public Map<String, Object> getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
      throw new RuntimeException("Usuário não autenticado");
    }

    return Map.of(
        "email", user.getEmail(),
        "name", user.getName(),
        "role", user.getRole().name(),
        "sections",
            user.getSections().stream()
                .map(section -> new SectionDto(section.getId(), section.getTitle()))
                .toList());
  }
}
