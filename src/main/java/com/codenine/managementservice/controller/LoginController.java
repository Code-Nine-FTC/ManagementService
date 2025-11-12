package com.codenine.managementservice.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import com.codenine.managementservice.dto.chat.ChatRoomDTO;
import com.codenine.managementservice.dto.login.LoginDto;
import com.codenine.managementservice.dto.login.LoginResponseDto;
import com.codenine.managementservice.dto.section.SectionDto;
import com.codenine.managementservice.entity.GuestUser;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.GuestUserRepository;
import com.codenine.managementservice.repository.UserRepository;
import com.codenine.managementservice.security.JwtUtil;
import com.codenine.managementservice.service.GuestUserService;
import com.codenine.managementservice.utils.NormalizeEmail;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/login")
public class LoginController {

  @Autowired private JwtUtil jwtUtil;

  @Autowired private AuthenticationManager authenticationManager;

  @Autowired private UserRepository userRepository;

  @Autowired private GuestUserRepository guestUserRepository;

  @Autowired private GuestUserService guestUserService;

  /**
   * Realiza o login do usuário.
   *
   * @param credentials Dados de login (email e senha).
   * @return Token JWT e dados do usuário autenticado, ou mensagem de erro.
   */
  @Operation(description = "Realiza o login do usuário ou guest.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "Credenciais de login (email e senha)")
  @PostMapping
  public ResponseEntity<?> login(@RequestBody LoginDto credentials) {
    String email = NormalizeEmail.normalize(credentials.email());
    String password = credentials.password();

    // Primeiro verifica se é um usuário normal
    Optional<User> userEmail = userRepository.findByEmail(email);

    if (userEmail.isPresent()) {
      User user = userEmail.get();
      if (!user.getIsActive()) {
        return ResponseEntity.status(403).body("Usuário não Autorizado");
      }

      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
      List<Long> sectionIds = user.getSections().stream().map(s -> s.getId()).toList();
      List<SectionDto> sections =
          user.getSections().stream().map(s -> new SectionDto(s.getId(), s.getTitle())).toList();
      String token = jwtUtil.generateToken(email, user.getRole(), sectionIds);
      return ResponseEntity.status(200)
          .body(
              new LoginResponseDto(
                  token,
                  user.getId(),
                  user.getName(),
                  email,
                  user.getRole().toString(),
                  sections));
    }

    // Se não for usuário normal, verifica se é guest
    Optional<GuestUser> guestEmail = guestUserRepository.findByEmail(email);

    if (guestEmail.isPresent()) {
      GuestUser guest = guestEmail.get();
      if (!guest.getIsActive()) {
        return ResponseEntity.status(403).body("Usuário convidado não autorizado");
      }

      // Autentica o guest
      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

      // Gera token específico para guest
      String token = jwtUtil.generateGuestToken(email, guest.getId());

      // Busca a sala de chat do guest
      ChatRoomDTO chatRoom = null;
      if (guest.getChatRoom() != null) {
        chatRoom = guestUserService.getGuestChatRoom(guest.getId());
      }

      // Retorna resposta customizada para guest
      return ResponseEntity.status(200)
          .body(
              new GuestLoginResponseDto(
                  token,
                  guest.getId(),
                  guest.getName(),
                  email,
                  "GUEST",
                  chatRoom));
    }

    return ResponseEntity.status(404).body("Usuário não encontrado");
  }

  // DTO interno para resposta de login de guest
  private record GuestLoginResponseDto(
      String token,
      Long id,
      String name,
      String email,
      String role,
      ChatRoomDTO chatRoom) {}
}
