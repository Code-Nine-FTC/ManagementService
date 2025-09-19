package com.codenine.managementservice.security;

import com.codenine.managementservice.dto.user.Role;
import com.codenine.managementservice.dto.user.UserRequest;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.UserRepository;
import com.codenine.managementservice.utils.exception.UserManagementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurityService {

    @Autowired
    private UserRepository userRepository;

    public boolean hasUserManagementPermission(Authentication authentication, Long userId)
            throws UserManagementException {
        User user = (User) authentication.getPrincipal();
        Role role = user.getRole();

        User targetUser = userRepository.findById(userId).orElse(null);
        if (targetUser == null) {
            throw new NullPointerException("Usuário alvo não encontrado");
        }

        if (role.equals(Role.ADMIN)) {
            return true;
        }

        if (role.equals(Role.ASSISTANT) || role.equals(Role.MANAGER)) {
            if (targetUser.getRole().equals(Role.ADMIN)) {
                throw new UserManagementException("Usuário não possui permissão para gerenciar administradores");
            }
            boolean hasPermission = user.getSections().stream()
                    .anyMatch(section -> targetUser.getSections().stream()
                    .anyMatch(targetSection -> targetSection.getId().equals(section.getId())));
            if (!hasPermission) {
                throw new UserManagementException("Usuário não possui permissão para gerenciar usuários desse setor");
            }
            return true;
        }
            return userRepository.existsById(userId);
    }

    public boolean hasUserRegisterPermission(Authentication authentication, UserRequest userRequest) {
        User user = (User) authentication.getPrincipal();
        Role role = user.getRole();
        switch (role) {
            case ADMIN:
                return true;
            case MANAGER:
                boolean hasPermission = user.getSections().stream()
                    .anyMatch(section -> userRequest.sectionIds().stream()
                    .anyMatch(requestSectionId -> requestSectionId.equals(section.getId())));
                if (!hasPermission) {
                    throw new UserManagementException("Usuário não possui permissão para registrar usuários nesse setor");
                }
                if (userRequest.role().equals(Role.ADMIN) || userRequest.role().equals(Role.MANAGER)) {
                    throw new UserManagementException("Usuário não possui permissão para registrar administradores e gerentes");
                }
                return true;
            default:
                return false;
        }
    }

    public boolean userViewPermission(Authentication authentication, Long userId) {
        User user = (User) authentication.getPrincipal();
        Role role = user.getRole();

        User targetUser = userRepository.findById(userId).orElse(null);
        if (targetUser == null) {
            throw new NullPointerException("Usuário alvo não encontrado");
        }

        switch (user.getRole()) {
            case ADMIN:
                return true;
            case MANAGER:
                if (targetUser.getRole().equals(Role.ADMIN)) {
                    throw new UserManagementException("Usuário não possui permissão para visualizar administradores");
                }
                if (targetUser.getRole().equals(Role.MANAGER) && !targetUser.getId().equals(user.getId())) {
                    throw new UserManagementException("Usuário não possui permissão para visualizar outros gerentes");
                }
                boolean hasPermission = user.getSections().stream()
                        .anyMatch(section -> targetUser.getSections().stream()
                                .anyMatch(targetSection -> targetSection.getId().equals(section.getId())));
                if (!hasPermission) {
                    throw new UserManagementException("Usuário não possui permissão para visualizar usuários desse setor");
                }
                return true;
            default:
                if (targetUser.getId().equals(user.getId())) {
                    return true;
                } else {
                    throw new UserManagementException("Usuário não possui permissão para visualizar outros usuários");
                }
        }
    }
}
