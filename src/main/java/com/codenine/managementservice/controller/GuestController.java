package com.codenine.managementservice.controller;

import com.codenine.managementservice.dto.chat.*;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.service.GuestUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Guest Users", description = "Endpoints para gerenciar usuários convidados (guests)")
public class GuestController {

  private final GuestUserService guestUserService;

  public GuestController(GuestUserService guestUserService) {
    this.guestUserService = guestUserService;
  }

  @PostMapping("/register-guest")
  @Operation(
      summary = "Registrar novo usuário guest",
      description =
          "Cadastra um novo usuário guest e cria automaticamente uma sala de chat. Requer permissão de ADMIN ou Farmácia (SECTION_2)")
  @PreAuthorize("hasRole('ADMIN') or hasAuthority('SECTION_2')")
  public ResponseEntity<ChatRoomDTO> registerGuest(
      @Valid @RequestBody CreateGuestRequest request, Authentication authentication) {

    System.out.println("=== DEBUG REGISTER GUEST ===");
    System.out.println("Request: " + request);
    System.out.println("Nome: " + request.getName());
    System.out.println("CPF: " + request.getCpf());
    System.out.println("Idade: " + request.getAge());
    System.out.println("Sexo: " + request.getGender());
    System.out.println("E-mail: " + request.getEmail());
    
    // Debug authorities
    System.out.println("Authorities: " + authentication.getAuthorities());
    System.out.println("===========================");

    User currentUser = (User) authentication.getPrincipal();
    ChatRoomDTO chatRoom = guestUserService.registerGuest(request, currentUser.getId());

    return ResponseEntity.status(HttpStatus.CREATED).body(chatRoom);
  }

  @PostMapping("/guest/login")
  @Operation(
      summary = "Login de usuário guest",
      description = "Autentica um usuário guest e retorna token JWT e informações da sala de chat")
  public ResponseEntity<GuestLoginResponse> loginGuest(
      @Valid @RequestBody GuestLoginRequest request) {

    System.out.println("=== DEBUG GUEST LOGIN ===");
    System.out.println("E-mail: " + request.getEmail());
    System.out.println("========================");

    GuestLoginResponse response = guestUserService.loginGuest(request);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/guest/profile")
  @Operation(
      summary = "Obter perfil do guest",
      description = "Retorna informações do perfil do usuário guest autenticado")
  @PreAuthorize("hasRole('GUEST')")
  public ResponseEntity<GuestUserDTO> getGuestProfile(Authentication authentication) {
    String email = authentication.getName();
    GuestUserDTO guestUser = guestUserService.getGuestByEmail(email);
    return ResponseEntity.ok(guestUser);
  }

  @GetMapping("/guest/{guestId}/chat-room")
  @Operation(
      summary = "Obter sala de chat do guest",
      description = "Retorna a sala de chat associada ao usuário guest")
  @PreAuthorize("hasRole('ADMIN') or hasAuthority('SECTION_2') or hasRole('GUEST')")
  public ResponseEntity<ChatRoomDTO> getGuestChatRoom(@PathVariable Long guestId) {
    ChatRoomDTO chatRoom = guestUserService.getGuestChatRoom(guestId);
    return ResponseEntity.ok(chatRoom);
  }
}
