package com.codenine.managementservice.security;

import com.codenine.managementservice.dto.Role;
import com.codenine.managementservice.entity.ItemType;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.ItemTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("itemSecurity")
public class ItemSecurityService {

    @Autowired
    private ItemTypeRepository itemTypeRepository;

    public boolean hasItemManagementPermission(Authentication authentication, Long itemTypeId) {
        User user = (User) authentication.getPrincipal();
        Role role = user.getRole();

        if (role.equals(Role.ADMIN) || role.equals(Role.MANAGER)) {
            return true;
        }
        if (role.equals(Role.ASSISTANT)) {
            ItemType itemType = itemTypeRepository.findById(itemTypeId).orElse(null);
            return user.getSection() != null && user.getSection().getId().equals(itemType.getSection().getId());
        }
        return false;
    }
}
