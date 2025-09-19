package com.codenine.managementservice.utils.mapper;

import com.codenine.managementservice.dto.itemLoss.ItemLossRequest;
import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.entity.ItemLoss;
import com.codenine.managementservice.entity.User;

public class ItemLossMapper {
  public static ItemLoss toEntity(
      ItemLossRequest request, Item item, User recordedBy, User lastUser) {
    ItemLoss itemLoss = new ItemLoss();
    itemLoss.setLostQuantity(request.lostQuantity());
    itemLoss.setReason(request.reason());
    itemLoss.setItem(item);
    itemLoss.setRecordedBy(recordedBy);
    itemLoss.setLastUser(lastUser);
    return itemLoss;
  }

  public static void updateEntity(
      ItemLoss itemLoss, ItemLossRequest request, Item item, User lastUser) {
    if (request.lostQuantity() != null) itemLoss.setLostQuantity(request.lostQuantity());
    if (request.reason() != null) itemLoss.setReason(request.reason());
    if (item != null) itemLoss.setItem(item);
    if (request.lostQuantity() != null || request.reason() != null) {
      itemLoss.setLastUpdate(java.time.LocalDateTime.now());
      itemLoss.setLastUser(lastUser);
    }
  }
}
