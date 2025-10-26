package com.codenine.managementservice.dto.item;

import java.time.LocalDateTime;
import java.util.List;

import com.codenine.managementservice.dto.itemLoss.ItemLossResponse;

public record ItemResponse(
    Long itemId,
    String name,
    Integer currentStock,
    String measure,
    LocalDateTime expireDate,
    Long sectionId,
    String sectionName,
    Long itemTypeId,
    String itemTypeName,
    Integer minimumStock,
    String qrCode,
    String itemCode,
    String lastUserName,
    LocalDateTime lastUpdate,
    List<ItemLossResponse> lossHistory) {

  public ItemResponse(
      Long itemId,
      String name,
      Integer currentStock,
      String measure,
      LocalDateTime expireDate,
      Long sectionId,
      String sectionName,
      Long itemTypeId,
      String itemTypeName,
      Integer minimumStock,
      String qrCode,
      String itemCode,
      String lastUserName,
      LocalDateTime lastUpdate) {
    this(
        itemId,
        name,
        currentStock,
        measure,
        expireDate,
        sectionId,
        sectionName,
        itemTypeId,
        itemTypeName,
        minimumStock,
        qrCode,
        itemCode,
        lastUserName,
        lastUpdate,
        List.of());
  }
}
