package com.codenine.managementservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.dto.ItemFilterCriteria;
import com.codenine.managementservice.dto.ItemRequest;
import com.codenine.managementservice.dto.ItemResponse;
import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.entity.ItemType;
import com.codenine.managementservice.entity.Section;
import com.codenine.managementservice.entity.SupplierCompany;

import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.mapper.ItemMapper;
import com.codenine.managementservice.repository.ItemRepository;
import com.codenine.managementservice.repository.ItemTypeRepository;
import com.codenine.managementservice.repository.SectionRepository;
import com.codenine.managementservice.repository.SupplierCompanyRepository;
import com.codenine.managementservice.repository.UserRepository;


@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private ItemTypeRepository itemTypeRepository;

    @Autowired
    private SupplierCompanyRepository supplierCompanyRepository;
    public void createItem(ItemRequest itemRequest) {
        User user = userRepository.findById(itemRequest.lastUserId())
                .orElseThrow(() -> new NullPointerException("User not found with id: " + itemRequest.lastUserId()));
        ItemType itemType = itemTypeRepository.findById(itemRequest.itemTypeId())
                .orElseThrow(() -> new NullPointerException("ItemType not found with id: " + itemRequest.itemTypeId()));
        SupplierCompany supplier = supplierCompanyRepository.findById(itemRequest.supplierId())
                .orElseThrow(() -> new NullPointerException("SupplierCompany not found with id: " + itemRequest.supplierId()));
        Item newItem = ItemMapper.toEntity(itemRequest, user, supplier, itemType);
        itemRepository.save(newItem);
    }

    public ItemResponse getItem(Long id) {
        getItemById(id);
        ItemResponse item = itemRepository.findAllItemResponses(null, null, null, null, null, id
        ).stream().findFirst().orElse(null);
        return item;
    }

    public List<ItemResponse> getItemsByFilter(ItemFilterCriteria filterCriteria) {
        return itemRepository.findAllItemResponses(
            filterCriteria.supplierId(),
            filterCriteria.sectionId(),
            filterCriteria.itemTypeId(),
            filterCriteria.lastUserId(),
            filterCriteria.isActive(),
            filterCriteria.itemId()
        );
    }

    public void updateItem(Long id, ItemRequest itemRequest) {
        Item item = getItemById(id);
        User lastUser = userRepository.findById(itemRequest.lastUserId())
                .orElseThrow(() -> new NullPointerException("User not found with id: " + itemRequest.lastUserId()));
        ItemMapper.updateEntity(item, itemRequest, lastUser, null);
        itemRepository.save(item);
    }

    public void disableItem(Long id) {
        Item item = getItemById(id);
        item.setIsActive(false);
        itemRepository.save(item);
    }
    
    private Item getItemById(Long id) {
        return itemRepository.findById(id).orElseThrow(() -> new NullPointerException("Item not found with id: " + id));
    }
}
