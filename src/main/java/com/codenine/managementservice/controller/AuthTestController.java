package com.codenine.managementservice.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codenine.managementservice.entity.User;

@RestController
public class AuthTestController {

  @GetMapping("/auth/me")
  public Map<String, Object> getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
      throw new RuntimeException("Usuário não autenticado");
    }

    return Map.of(
        "email", user.getEmail(),
        "role", user.getRole().name(),
        "sections", user.getSections().stream().map(section -> section.getId()).toList());
  }
}
