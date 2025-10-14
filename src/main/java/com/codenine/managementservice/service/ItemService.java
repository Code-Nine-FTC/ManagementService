package com.codenine.managementservice.service;

import java.time.LocalDateTime;
import java.util.List;

import com.codenine.managementservice.utils.CryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.dto.item.ArchiveItem;
import com.codenine.managementservice.dto.item.ItemFilterCriteria;
import com.codenine.managementservice.dto.item.ItemRequest;
import com.codenine.managementservice.dto.item.ItemResponse;
import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.entity.ItemType;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.ItemRepository;
import com.codenine.managementservice.repository.ItemTypeRepository;
import com.codenine.managementservice.utils.mapper.ItemMapper;

@Service
public class ItemService {
  @Autowired private ItemRepository itemRepository;

  @Autowired private ItemTypeRepository itemTypeRepository;

  @Autowired private CryptUtil cryptUtil;

  public void createItem(ItemRequest itemRequest, User lastUser) {
    ItemType itemType =
        itemTypeRepository
            .findById(itemRequest.itemTypeId())
            .orElseThrow(
                () ->
                    new NullPointerException(
                        "ItemType not found with id: " + itemRequest.itemTypeId()));
    Item newItem = ItemMapper.toEntity(itemRequest, lastUser, itemType);
    Item savedItem = itemRepository.save(newItem);
    String qrCode = cryptUtil.encrypt(savedItem.getId().toString());
    savedItem.setQrCode("/items/qr?code=" + qrCode);
    itemRepository.save(savedItem);
  }

  public ItemResponse getItem(Long id) {
    getItemById(id);
    return itemRepository.findAllItemResponses(null, null, null, null, id).stream()
        .findFirst()
        .orElse(null);
  }

  public List<ItemResponse> getItemsByFilter(ItemFilterCriteria filterCriteria) {
    return itemRepository.findAllItemResponses(
        filterCriteria.itemCode(),
        filterCriteria.sectionId(),
        filterCriteria.itemTypeId(),
        filterCriteria.isActive(),
        filterCriteria.itemId());
  }

  public ItemResponse getEncryptedItem(String qrCode) {
    String decryptedId = cryptUtil.decrypt(qrCode);
    Long itemId;
    try {
      itemId = Long.valueOf(decryptedId);
        return getItem(itemId);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid QR code");
    }
  }

  public void updateItem(Long id, ItemRequest itemRequest, User lastUser) {
    Item item = getItemById(id);
    ItemType itemType = null;
    if (itemRequest.itemTypeId() != null) {
      itemType =
          itemTypeRepository
              .findById(itemRequest.itemTypeId())
              .orElseThrow(
                  () ->
                      new NullPointerException(
                          "ItemType not found with id: " + itemRequest.itemTypeId()));
    }
    ItemMapper.updateEntity(item, itemRequest, lastUser, itemType);
    itemRepository.save(item);
  }

  public void disableItem(Long id, User lastUser) {
    Item item = getItemById(id);
    item.setIsActive(false);
    item.setLastUpdate(LocalDateTime.now());
    item.setLastUser(lastUser);
    itemRepository.save(item);
  }

  public void archiveItem(Long id, ArchiveItem archiveItem, User lastUser) {
    Item item = getItemById(id);
    item.setArchiveInfo("{\"status\": true, \"reason\": \"" + archiveItem.reason() + "\"}");
    item.setLastUpdate(LocalDateTime.now());
    item.setLastUser(lastUser);
    itemRepository.save(item);
  }

  private Item getItemById(Long id) {
    return itemRepository
        .findById(id)
        .orElseThrow(() -> new NullPointerException("Item not found with id: " + id));
  }
}
