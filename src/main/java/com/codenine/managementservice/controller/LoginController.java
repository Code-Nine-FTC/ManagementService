package com.codenine.managementservice.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import com.codenine.managementservice.dto.login.LoginDto;
import com.codenine.managementservice.dto.login.LoginResponseDto;
import com.codenine.managementservice.dto.section.SectionDto;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.UserRepository;
import com.codenine.managementservice.security.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/login")
public class LoginController {

  @Autowired private JwtUtil jwtUtil;

  @Autowired private AuthenticationManager authenticationManager;

  @Autowired private UserRepository userRepository;

  /**
   * Realiza o login do usuário.
   *
   * @param credentials Dados de login (email e senha).
   * @return Token JWT e dados do usuário autenticado, ou mensagem de erro.
   */
  @Operation(description = "Realiza o login do usuário.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "Credenciais de login (email e senha)")
  @PostMapping
  public ResponseEntity<?> login(
      @org.springframework.web.bind.annotation.RequestBody LoginDto credentials) {
    String email = credentials.email();
    String password = credentials.password();

    Optional<User> userEmail = userRepository.findByEmail(email);
    if (userEmail.isPresent()) {
      User user = userEmail.get();
      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

      List<Long> sectionIds = user.getSections().stream().map(s -> s.getId()).toList();

      List<SectionDto> sections =
          user.getSections().stream().map(s -> new SectionDto(s.getId(), s.getTitle())).toList();

      String token = jwtUtil.generateToken(email, user.getRole(), sectionIds);

      return ResponseEntity.status(200)
          .body(
              new LoginResponseDto(
                  token, user.getId(), user.getName(), email, user.getRole().toString(), sections));
    } else {
      return ResponseEntity.status(404).body("Usuário não encontrado");
    }
  }
}
