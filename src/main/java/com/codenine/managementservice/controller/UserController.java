package com.codenine.managementservice.controller;

import com.codenine.managementservice.dto.user.Role;
import com.codenine.managementservice.dto.user.UserRequest;
import com.codenine.managementservice.dto.user.UserUpdate;
import com.codenine.managementservice.service.UserService;
import com.codenine.managementservice.utils.exception.UserManagementException;
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
        } catch (UserManagementException e) {
            return ResponseEntity.status(403).body(e.getMessage());
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

    @PatchMapping("/enable/{id}")
    @PreAuthorize("@userSecurity.hasUserManagementPermission(authentication, #id)")
    public ResponseEntity<String> enableUser(@PathVariable Long id) {
        try {
            userService.enableUser(id);
            return ResponseEntity.ok("User enabled successfully");
        } catch (NullPointerException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error enabling user: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("@userSecurity.hasUserManagementPermission(authentication, #id)")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody UserUpdate userRequest) {
        try {
            userService.updateUser(id, userRequest);
            return ResponseEntity.ok("User updated successfully");
        } catch (NullPointerException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating user: " + e.getMessage());
        }
    }

    @PatchMapping("/role/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateUserRole(@PathVariable Long id, @RequestParam Role role) {
        try {
            userService.updateRole(id, role);
            return ResponseEntity.ok("User role updated successfully");
        } catch (NullPointerException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating user role: " + e.getMessage());
        }
    }

    @PatchMapping("/sections/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<String> updateUserSections(@PathVariable Long id, @RequestBody List<Long> sectionIds) {
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
