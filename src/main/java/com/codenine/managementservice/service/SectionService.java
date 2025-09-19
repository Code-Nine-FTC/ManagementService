package com.codenine.managementservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.entity.Section;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.SectionRepository;
import com.codenine.managementservice.repository.UserRepository;

@Service
public class SectionService {

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private UserRepository userRepository;
    

    // Implementar CRUD

    private Section getSectionById(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new NullPointerException("Section not found with id: " + id));
    }

    private User getLastUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NullPointerException("User not found with id: " + id));
    }
}
