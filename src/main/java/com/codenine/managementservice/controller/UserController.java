package com.codenine.managementservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.codenine.managementservice.dto.user.Role;
import com.codenine.managementservice.dto.user.UserRequest;
import com.codenine.managementservice.dto.user.UserUpdate;
import com.codenine.managementservice.service.UserService;
import com.codenine.managementservice.utils.exception.UserManagementException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/users")
public class UserController {

  @Autowired
  private UserService userService;

  /**
   * Cria um novo usuário no sistema.
   *
   * @param userRequest Dados do usuário a ser criado.
   * @return Mensagem de sucesso ou erro.
   */
  @Operation(description = "Cria um novo usuário no sistema.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do usuário a ser criado")
  @PreAuthorize("@userSecurity.hasUserRegisterPermission(authentication, #userRequest)")
  @PostMapping
  public ResponseEntity<String> createUser(
      @RequestBody UserRequest userRequest) {
    try {
      userService.createUser(userRequest);
      return ResponseEntity.status(201).body("User created successfully");
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error creating user: " + e.getMessage());
    }
  }

  /**
   * Busca um usuário pelo ID.
   *
   * @param id ID do usuário.
   * @return Dados do usuário ou mensagem de erro.
   */
  @Operation(description = "Busca um usuário pelo ID.")
  @PreAuthorize("@userSecurity.hasUserViewPermission(authentication, #id)")
  @GetMapping("/{id}")
  public ResponseEntity<?> getUser(
      @Parameter(description = "ID do usuário a ser buscado", example = "1") @PathVariable Long id) {
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
   *
   * @return Lista de usuários.
   */
  @Operation(description = "Lista todos os usuários do sistema.")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @GetMapping
  public ResponseEntity<?> getAllUsers(@Parameter(hidden = true) Authentication authentication) {
    try {
      var users = userService.getAllUsers(authentication);
      return ResponseEntity.ok(users);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error retrieving users: " + e.getMessage());
    }
  }

  /**
   * Ativa ou desativa um usuário pelo ID.
   *
   * @param id ID do usuário.
   * @return Mensagem de sucesso ou erro.
   */

  @Operation(description = "Ativa ou desativa um usuário pelo ID.")
  @PatchMapping("/switch/{id}")
  @PreAuthorize("@userSecurity.hasUserManagementPermission(authentication, #id)")
  public ResponseEntity<String> switchUserActive(
      @Parameter(description = "ID do usuário a ser ativado/desativado", example = "1") @PathVariable Long id) {
    try {
      userService.switchUserActive(id);
      return ResponseEntity.ok("User status switched successfully");
    } catch (NullPointerException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error switching user status: " + e.getMessage());
    }
  }

  /**
   * Atualiza os dados de um usuário pelo ID.
   *
   * @param id          ID do usuário.
   * @param userRequest Novos dados do usuário.
   * @return Mensagem de sucesso ou erro.
   */
  @Operation(description = "Atualiza os dados de um usuário pelo ID.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Novos dados do usuário")
  @PutMapping("/{id}")
  @PreAuthorize("@userSecurity.hasUserManagementPermission(authentication, #id)")
  public ResponseEntity<String> updateUser(
      @Parameter(description = "ID do usuário a ser atualizado", example = "1") @PathVariable Long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Novos dados do usuário") @RequestBody UserRequest userRequest) {
    try {
      userService.updateUser(id, userRequest);
      return ResponseEntity.ok("User updated successfully");
    } catch (NullPointerException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error updating user: " + e.getMessage());
    }
  }

}
