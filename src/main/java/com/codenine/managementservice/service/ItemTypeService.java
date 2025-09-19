package com.codenine.managementservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.dto.itemType.ItemTypeFilterCriteria;
import com.codenine.managementservice.dto.itemType.ItemTypeRequest;
import com.codenine.managementservice.dto.itemType.ItemTypeResponse;
import com.codenine.managementservice.entity.ItemType;
import com.codenine.managementservice.entity.Section;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.mapper.ItemTypeMapper;
import com.codenine.managementservice.repository.ItemTypeRepository;
import com.codenine.managementservice.repository.SectionRepository;

@Service
public class ItemTypeService {
    @Autowired
    private ItemTypeRepository itemTypeRepository;

    @Autowired
    private SectionRepository sectionRepository;

    public void createItemType(ItemTypeRequest newItemType, User lastUser) {
        if (newItemType.sectionId() == null) {
            throw new NullPointerException("Section ID is required to create an ItemType.");
        }
        Section section = sectionRepository.findById(newItemType.sectionId())
                .orElseThrow(() -> new NullPointerException("Section not found with id: " + newItemType.sectionId()));
        ItemType itemType = ItemTypeMapper.toEntity(newItemType, section, lastUser);
        itemTypeRepository.save(itemType);
    }

    public void updateItemType(Long id, ItemTypeRequest updatedItemType, User lastUser) {
        ItemType itemType = getItemTypeById(id);
        Section section = null;
        if (updatedItemType.sectionId() != null) {
            section = sectionRepository.findById(updatedItemType.sectionId())
                    .orElseThrow(() -> new NullPointerException(
                            "Section not found with id: " + updatedItemType.sectionId()));
        }
        ItemTypeMapper.updateEntity(itemType, updatedItemType, section, lastUser);
        itemTypeRepository.save(itemType);
    }

    public ItemTypeResponse getItemType(Long id) {
        return itemTypeRepository.findAllItemTypeResponses(id, null, null)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NullPointerException("ItemType not found with id: " + id));
    }

    public List<ItemTypeResponse> getItemTypesByFilter(ItemTypeFilterCriteria filterCriteria) {
        return itemTypeRepository.findAllItemTypeResponses(
                filterCriteria.itemTypeId(),
                filterCriteria.sectionId(),
                filterCriteria.lastUserId());
    }

    public void disableItemType(Long id, User lastUser) {
        ItemType itemType = getItemTypeById(id);
        itemType.setIsActive(false);
        itemType.setLastUser(lastUser);
        itemType.setLastUpdate(java.time.LocalDateTime.now());
        itemTypeRepository.save(itemType);
    }

    private ItemType getItemTypeById(Long id) {
        return itemTypeRepository.findById(id)
                .orElseThrow(() -> new NullPointerException("ItemType not found with id: " + id));
    }

}
