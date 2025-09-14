package com.codenine.managementservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.dto.ItemFilterCriteria;
import com.codenine.managementservice.dto.ItemRequest;
import com.codenine.managementservice.dto.ItemResponse;
import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.mapper.ItemMapper;
import com.codenine.managementservice.repository.ItemRepository;


@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;

    public void createItem(ItemRequest itemRequest) {
        Item newItem = ItemMapper.toEntity(itemRequest);
        itemRepository.save(newItem);
    }

    public ItemResponse getItem(Integer id) {
        if (getItemById(id) == null) throw new NullPointerException("Item not found with id: " + id);
        ItemResponse item = itemRepository.findAllItemResponses(null, null, null, null, null, id
        ).stream().findFirst().orElse(null);
        return item;
    }

    public List<ItemResponse> getItemsByFilter(ItemFilterCriteria filterCriteria) {
        return itemRepository.findAllItemResponses(
            filterCriteria.supplierId(),
            filterCriteria.sectionId(),
            filterCriteria.typeItemId(),
            filterCriteria.lastUserId(),
            filterCriteria.isActive(),
            filterCriteria.itemId()
        );
    }

    public void updateItem(Integer id, ItemRequest itemRequest) {
        Item item = getItemById(id);
        if (item == null) throw new NullPointerException("Item not found with id: " + id);
        ItemMapper.updateEntity(item, itemRequest);
        itemRepository.save(item);
    }

    public void disableItem(Integer id) {
        Item item = getItemById(id);
        if (item == null) throw new NullPointerException("Item not found with id: " + id);
        item.setIsActive(false);
        itemRepository.save(item);
    }
    
    private Item getItemById(Integer id) {
        return itemRepository.findById(id).orElse(null);
    }
}
