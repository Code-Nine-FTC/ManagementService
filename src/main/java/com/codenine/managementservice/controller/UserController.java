package com.codenine.managementservice.controller;

import com.codenine.managementservice.dto.user.UserRequest;
import com.codenine.managementservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PreAuthorize("@userSecurity.hasUserRegisterPermission(authentication, #userRequest)")
    @PostMapping("/")
    public ResponseEntity<String> createUser(@RequestBody UserRequest userRequest) {
        try {
            userService.createUser(userRequest);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating user: " + e.getMessage());
        }
        return ResponseEntity.status(201).body("User created successfully");
    }

    @PreAuthorize("@userSecurity.hasUserViewPermission(authentication, #id)")
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        try {
            var user = userService.getUser(id);
            return ResponseEntity.ok(user);
        } catch (NullPointerException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving user: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/")
    public ResponseEntity<?> getAllUsers(Authentication authentication) {
        try {
            var users = userService.getAllUsers(authentication);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving users: " + e.getMessage());
        }
    }

    @PatchMapping("/disable/{id}")
    @PreAuthorize("@userSecurity.hasUserManagementPermission(authentication, #id)")
    public ResponseEntity<String> disableUser(@PathVariable Long id) {
        try {
            userService.disableUser(id);
            return ResponseEntity.ok("User disabled successfully");
        } catch (NullPointerException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error disabling user: " + e.getMessage());
        }
    }
}
