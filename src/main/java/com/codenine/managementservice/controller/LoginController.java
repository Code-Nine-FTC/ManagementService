package com.codenine.managementservice.controller;

import com.codenine.managementservice.dto.LoginDto;
import com.codenine.managementservice.dto.LoginResponseDto;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.UserRepository;
import com.codenine.managementservice.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> login(@RequestBody LoginDto credentials) {
        String email = credentials.email();
        String password = credentials.password();

        Optional<User> userEmail = userRepository.findByEmail(email);
        if (userEmail.isPresent()) {
            User user = userEmail.get();
            if (user.getPassword().equals(password)) {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(email, password)
                );

                String token = jwtUtil.generateToken(email, user.getRole(), user.getSection().getId());

                return ResponseEntity.status(200).body(
                        new LoginResponseDto(
                                token,
                                email,
                                user.getRole().toString(),
                                user.getSection().getId(),
                                user.getSection().getTitle()
                        )
                );
            } else {
                return ResponseEntity.status(401).body("Credenciais inválidas");
            }
        }
        return ResponseEntity.status(404).body("Usuário não encontrado");
    }
}