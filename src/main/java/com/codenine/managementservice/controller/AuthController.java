package com.codenine.managementservice.controller;

import com.codenine.managementservice.dto.chat.GuestUserDTO;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.security.GuestUserDetails;
import com.codenine.managementservice.service.GuestUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints de autenticação")
public class AuthController {

  private final GuestUserService guestUserService;

  public AuthController(GuestUserService guestUserService) {
    this.guestUserService = guestUserService;
  }

  @GetMapping("/me")
  @Operation(
      summary = "Obter informações do usuário autenticado",
      description =
          "Retorna informações do usuário autenticado (normal ou guest) para o frontend saber como redirecionar")
  public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
    Map<String, Object> response = new HashMap<>();

    Object principal = authentication.getPrincipal();

    if (principal instanceof GuestUserDetails) {
      // É um guest user
      GuestUserDetails guestDetails = (GuestUserDetails) principal;
      GuestUserDTO guestUser = guestUserService.getGuestByEmail(guestDetails.getUsername());

      response.put("userType", "GUEST");
      response.put("user", guestUser);
      response.put("chatRoomId", guestUser.getChatRoomId());

    } else if (principal instanceof User) {
      // É um usuário normal
      User user = (User) principal;

      Map<String, Object> userInfo = new HashMap<>();
      userInfo.put("id", user.getId());
      userInfo.put("name", user.getName());
      userInfo.put("email", user.getEmail());
      userInfo.put("role", user.getRole());
      userInfo.put("isActive", user.getIsActive());

      response.put("userType", "USER");
      response.put("user", userInfo);
    }

    return ResponseEntity.ok(response);
  }
}
