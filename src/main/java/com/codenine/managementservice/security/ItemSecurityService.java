package com.codenine.managementservice.security;

import com.codenine.managementservice.dto.user.Role;
import com.codenine.managementservice.entity.ItemType;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.exception.UserSectionMismatchException;
import com.codenine.managementservice.repository.ItemTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("itemSecurity")
public class ItemSecurityService {

    @Autowired
    private ItemTypeRepository itemTypeRepository;


    public boolean hasItemManagementPermission(Authentication authentication, Long itemTypeId)
            throws UserSectionMismatchException {
        User user = (User) authentication.getPrincipal();
        Role role = user.getRole();

        if (role.equals(Role.ADMIN)) {
            return true;
        }
        if (role.equals(Role.ASSISTANT) || role.equals(Role.MANAGER)) {
            ItemType itemType = itemTypeRepository.findById(itemTypeId).orElse(null);
            if (itemType == null)
                return false;
            boolean hasPermission = user.getSections().stream()
                    .anyMatch(section -> section.getId().equals(itemType.getSection().getId()));
            System.out.println("Boolean: " + hasPermission);
            if (!hasPermission) {
                throw new UserSectionMismatchException("Usuário não possui permissão para gerenciar itens desse setor");
            }
            return true;
        }
        return false;
    }
}
