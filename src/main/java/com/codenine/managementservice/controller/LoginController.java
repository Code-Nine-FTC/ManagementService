package com.codenine.managementservice.controller;

import com.codenine.managementservice.dto.login.LoginDto;
import com.codenine.managementservice.dto.login.LoginResponseDto;
import com.codenine.managementservice.entity.Section;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.UserRepository;
import com.codenine.managementservice.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            List<Long> sectionIds = user.getSections().stream()
                    .map(Section::getId)
                    .toList();

            String token = jwtUtil.generateToken(email, user.getRole(), sectionIds);

            return ResponseEntity.status(200).body(
                    new LoginResponseDto(
                            token,
                            email,
                            user.getRole().toString(),
                            sectionIds));
        } else {
            return ResponseEntity.status(404).body("Usuário não encontrado");
        }
    }
}