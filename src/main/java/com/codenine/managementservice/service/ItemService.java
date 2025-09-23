package com.codenine.managementservice.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.dto.item.ArchiveItem;
import com.codenine.managementservice.dto.item.ItemFilterCriteria;
import com.codenine.managementservice.dto.item.ItemRequest;
import com.codenine.managementservice.dto.item.ItemResponse;
import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.entity.ItemType;
import com.codenine.managementservice.entity.SupplierCompany;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.ItemRepository;
import com.codenine.managementservice.repository.ItemTypeRepository;
import com.codenine.managementservice.repository.SupplierCompanyRepository;
import com.codenine.managementservice.utils.mapper.ItemMapper;

@Service
public class ItemService {
  @Autowired private ItemRepository itemRepository;

  @Autowired private ItemTypeRepository itemTypeRepository;

  @Autowired private SupplierCompanyRepository supplierCompanyRepository;

  public void createItem(ItemRequest itemRequest, User lastUser) {
    ItemType itemType =
        itemTypeRepository
            .findById(itemRequest.itemTypeId())
            .orElseThrow(
                () ->
                    new NullPointerException(
                        "ItemType not found with id: " + itemRequest.itemTypeId()));
    SupplierCompany supplier =
        supplierCompanyRepository
            .findById(itemRequest.supplierId())
            .orElseThrow(
                () ->
                    new NullPointerException(
                        "SupplierCompany not found with id: " + itemRequest.supplierId()));
    Item newItem = ItemMapper.toEntity(itemRequest, lastUser, supplier, itemType);
    itemRepository.save(newItem);
  }

  public ItemResponse getItem(Long id) {
    getItemById(id);
    return itemRepository.findAllItemResponses(null, null, null, null, null, id).stream()
        .findFirst()
        .orElse(null);
  }

  public List<ItemResponse> getItemsByFilter(ItemFilterCriteria filterCriteria) {
    return itemRepository.findAllItemResponses(
        filterCriteria.supplierId(),
        filterCriteria.sectionId(),
        filterCriteria.itemTypeId(),
        filterCriteria.lastUserId(),
        filterCriteria.isActive(),
        filterCriteria.itemId());
  }

  public void updateItem(Long id, ItemRequest itemRequest, User lastUser) {
    Item item = getItemById(id);
    ItemType itemType = null;
    SupplierCompany supplier = null;
    if (itemRequest.itemTypeId() != null) {
      itemType =
          itemTypeRepository
              .findById(itemRequest.itemTypeId())
              .orElseThrow(
                  () ->
                      new NullPointerException(
                          "ItemType not found with id: " + itemRequest.itemTypeId()));
    }
    if (itemRequest.supplierId() != null) {
      supplier =
          supplierCompanyRepository
              .findById(itemRequest.supplierId())
              .orElseThrow(
                  () ->
                      new NullPointerException(
                          "SupplierCompany not found with id: " + itemRequest.supplierId()));
    }
    ItemMapper.updateEntity(item, itemRequest, lastUser, supplier, itemType);
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
