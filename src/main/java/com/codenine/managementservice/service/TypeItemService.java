package com.codenine.managementservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.entity.TypeItem;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.TypeItemRepository;
import com.codenine.managementservice.repository.UserRepository;

@Service
public class TypeItemService {
    @Autowired
    private TypeItemRepository typeItemRepository;

    @Autowired
    private UserRepository userRepository;

    // Implementar CRUD

    private TypeItem getTypeItemById(Long id) {
        return typeItemRepository.findById(id)
                .orElseThrow(() -> new NullPointerException("TypeItem not found with id: " + id));
    }

    private User getLastUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NullPointerException("User not found with id: " + id));
    }
}

