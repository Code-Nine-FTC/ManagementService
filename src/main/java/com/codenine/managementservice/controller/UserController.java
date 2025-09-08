package com.codenine.managementservice.controller;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
//    @GetMapping("/example")
//    public ResponseEntity<String> example(HttpServletRequest request) {
//        Claims claims = (Claims) request.getAttribute("claims");
//        Long role = claims.get("role", Long.class);
//        Long sectionId = claims.get("sectionId", Long.class);
//
//        if (role == 1) {
//            return ResponseEntity.ok("Acesso total para administradores.");
//        } else if (role == 3 && sectionId != null) {
//            return ResponseEntity.ok("Acesso permitido para gerenciadores do setor " + sectionId);
//        } else if (role == 5 && sectionId != null) {
//            return ResponseEntity.ok("Acesso limitado para encarregados do setor " + sectionId);
//        } else {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado.");
//        }
//    }
}
