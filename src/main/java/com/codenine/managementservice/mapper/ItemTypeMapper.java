package com.codenine.managementservice.mapper;

import com.codenine.managementservice.dto.itemType.ItemTypeRequest;
import com.codenine.managementservice.entity.ItemType;
import com.codenine.managementservice.entity.Section;
import com.codenine.managementservice.entity.User;

public class ItemTypeMapper {
    public static ItemType toEntity(ItemTypeRequest request, Section section, User lastUser) {
        ItemType itemType = new ItemType();
        itemType.setName(request.name());
        itemType.setSection(section);
        itemType.setLastUser(lastUser);
        return itemType;
    }

    public static void updateEntity(ItemType itemType, ItemTypeRequest request, Section section, User lastUser) {
        if (request.name() != null)
            itemType.setName(request.name());
        if (section != null)
            itemType.setSection(section);
        if (request.name() != null || section != null) {
            itemType.setLastUpdate(java.time.LocalDateTime.now());
            itemType.setLastUser(lastUser);
        }
    }
}
