package com.codenine.managementservice.utils.mapper;

import com.codenine.managementservice.dto.item.ItemRequest;
import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.entity.ItemType;
import com.codenine.managementservice.entity.User;

public class ItemMapper {
  public static Item toEntity(
      ItemRequest dto, User lastUser, ItemType itemType) {
    Item item = new Item();
    item.setName(dto.name());
    item.setCurrentStock(dto.currentStock());
    item.setMeasure(dto.measure());
    item.setExpireDate(dto.expireDate());
    item.setMinimumStock(dto.minimumStock());
    item.setQrCode(null);
    item.setLastUser(lastUser);
    if (dto.maximumStock() != null)
      item.setMaximumStock(dto.maximumStock());
    if (dto.isActive() != null)
      item.setIsActive(dto.isActive());
    item.setItemType(itemType);
    return item;
  }

  public static void updateEntity(
      Item item, ItemRequest dto, User lastUser, ItemType itemType) {
    if (dto.name() != null)
      item.setName(dto.name());
    if (dto.currentStock() != null)
      item.setCurrentStock(dto.currentStock());
    if (dto.measure() != null)
      item.setMeasure(dto.measure());
    if (dto.expireDate() != null)
      item.setExpireDate(dto.expireDate());
    if (dto.minimumStock() != null)
      item.setMinimumStock(dto.minimumStock());
    if (itemType != null)
      item.setItemType(itemType);
    if (dto.isActive() != null)
      item.setIsActive(dto.isActive());
    if (dto.maximumStock() != null)
      item.setMaximumStock(dto.maximumStock());
    if (dto.name() != null
        || dto.currentStock() != null
        || dto.measure() != null
        || dto.expireDate() != null
        || dto.minimumStock() != null
        || itemType != null
        || dto.isActive() != null
        || dto.maximumStock() != null) {
      item.setLastUpdate(java.time.LocalDateTime.now());
      item.setLastUser(lastUser);
    }
  }
}
