package com.codenine.managementservice.controller;

import com.codenine.managementservice.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public Map<String, String> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        // Valide o usuário (exemplo simplificado)
        Long role = 1L; // Obtenha do banco
        Long sectionId = null; // Obtenha do banco

        String token = jwtUtil.generateToken(email, role, sectionId);

        return Map.of("token", token, "role", role.toString(), "sectionId", sectionId != null ? sectionId.toString() : "null");
    }
}