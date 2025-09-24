package com.codenine.managementservice.controller;

import com.codenine.managementservice.dto.user.Role;
import com.codenine.managementservice.dto.user.UserRequest;
import com.codenine.managementservice.dto.user.UserUpdate;
import com.codenine.managementservice.service.UserService;
import com.codenine.managementservice.utils.exception.UserManagementException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Cria um novo usuário no sistema.
     * @param userRequest Dados do usuário a ser criado.
     * @return Mensagem de sucesso ou erro.
     */
    @Operation(description = "Cria um novo usuário no sistema.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do usuário a ser criado")
    @PreAuthorize("@userSecurity.hasUserRegisterPermission(authentication, #userRequest)")
    @PostMapping
    public ResponseEntity<String> createUser(
        @org.springframework.web.bind.annotation.RequestBody UserRequest userRequest) {
        try {
            userService.createUser(userRequest);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating user: " + e.getMessage());
        }
        return ResponseEntity.status(201).body("User created successfully");
    }

    /**
     * Busca um usuário pelo ID.
     * @param id ID do usuário.
     * @return Dados do usuário ou mensagem de erro.
     */
    @Operation(description = "Busca um usuário pelo ID.")
    @PreAuthorize("@userSecurity.hasUserViewPermission(authentication, #id)")
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(
        @Parameter(description = "ID do usuário a ser buscado", example = "1")
        @PathVariable Long id) {
        try {
            var user = userService.getUser(id);
            return ResponseEntity.ok(user);
        } catch (UserManagementException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving user: " + e.getMessage());
        }
    }

    /**
     * Lista todos os usuários do sistema.
     * @return Lista de usuários.
     */
    @Operation(description = "Lista todos os usuários do sistema.")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping
    public ResponseEntity<?> getAllUsers(
        @Parameter(hidden = true)
        Authentication authentication) {
        try {
            var users = userService.getAllUsers(authentication);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving users: " + e.getMessage());
        }
    }

    /**
     * Desativa um usuário pelo ID.
     * @param id ID do usuário.
     * @return Mensagem de sucesso ou erro.
     */
    @Operation(description = "Desativa um usuário pelo ID.")
    @PatchMapping("/disable/{id}")
    @PreAuthorize("@userSecurity.hasUserManagementPermission(authentication, #id)")
    public ResponseEntity<String> disableUser(
        @Parameter(description = "ID do usuário a ser desativado", example = "1")
        @PathVariable Long id) {
        try {
            userService.disableUser(id);
            return ResponseEntity.ok("User disabled successfully");
        } catch (NullPointerException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error disabling user: " + e.getMessage());
        }
    }

    /**
     * Habilita um usuário pelo ID.
     * @param id ID do usuário.
     * @return Mensagem de sucesso ou erro.
     */
    @Operation(description = "Habilita um usuário pelo ID.")
    @PatchMapping("/enable/{id}")
    @PreAuthorize("@userSecurity.hasUserManagementPermission(authentication, #id)")
    public ResponseEntity<String> enableUser(
        @Parameter(description = "ID do usuário a ser habilitado", example = "1")
        @PathVariable Long id) {
        try {
            userService.enableUser(id);
            return ResponseEntity.ok("User enabled successfully");
        } catch (NullPointerException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error enabling user: " + e.getMessage());
        }
    }

    /**
     * Atualiza os dados de um usuário pelo ID.
     * @param id ID do usuário.
     * @param userRequest Novos dados do usuário.
     * @return Mensagem de sucesso ou erro.
     */
    @Operation(description = "Atualiza os dados de um usuário pelo ID.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Novos dados do usuário")
    @PutMapping("/{id}")
    @PreAuthorize("@userSecurity.hasUserManagementPermission(authentication, #id)")
    public ResponseEntity<String> updateUser(
        @Parameter(description = "ID do usuário a ser atualizado", example = "1")
        @PathVariable Long id,
        @org.springframework.web.bind.annotation.RequestBody UserUpdate userRequest) {
        try {
            userService.updateUser(id, userRequest);
            return ResponseEntity.ok("User updated successfully");
        } catch (NullPointerException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating user: " + e.getMessage());
        }
    }

    /**
     * Atualiza o papel de um usuário pelo ID.
     * @param id ID do usuário.
     * @param role Novo papel do usuário.
     * @return Mensagem de sucesso ou erro.
     */
    @Operation(description = "Atualiza o papel de um usuário pelo ID.")
    @PatchMapping("/role/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateUserRole(
        @Parameter(description = "ID do usuário a ter o papel atualizado", example = "1")
        @PathVariable Long id,
        @Parameter(description = "Novo papel do usuário", example = "ADMIN")
        @RequestParam Role role) {
        try {
            userService.updateRole(id, role);
            return ResponseEntity.ok("User role updated successfully");
        } catch (NullPointerException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating user role: " + e.getMessage());
        }
    }

    /**
     * Atualiza as seções de um usuário pelo ID.
     * @param id ID do usuário.
     * @param sectionIds Novas seções do usuário.
     * @return Mensagem de sucesso ou erro.
     */
    @Operation(description = "Atualiza as seções de um usuário pelo ID.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Lista de IDs das novas seções do usuário")
    @PatchMapping("/sections/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<String> updateUserSections(
        @Parameter(description = "ID do usuário a ter as seções atualizadas", example = "1")
        @PathVariable Long id,
        @org.springframework.web.bind.annotation.RequestBody List<Long> sectionIds) {
        try {
            userService.updateSections(id, sectionIds);
            return ResponseEntity.ok("User sections updated successfully");
        } catch (NullPointerException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating user sections: " + e.getMessage());
        }
    }
}
