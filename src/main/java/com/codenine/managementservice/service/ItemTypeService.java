package com.codenine.managementservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.entity.ItemType;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.ItemTypeRepository;
import com.codenine.managementservice.repository.UserRepository;

@Service
public class ItemTypeService {
    @Autowired
    private ItemTypeRepository itemTypeRepository;

    @Autowired
    private UserRepository userRepository;

    // Implementar CRUD

    private ItemType getItemTypeById(Long id) {
        return itemTypeRepository.findById(id)
                .orElseThrow(() -> new NullPointerException("ItemType not found with id: " + id));
    }

    private User getLastUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NullPointerException("User not found with id: " + id));
    }
}

