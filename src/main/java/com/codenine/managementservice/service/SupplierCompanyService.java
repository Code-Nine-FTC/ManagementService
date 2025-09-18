package com.codenine.managementservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.entity.SupplierCompany;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.SupplierCompanyRepository;
import com.codenine.managementservice.repository.UserRepository;

@Service
public class SupplierCompanyService {
    @Autowired
    private SupplierCompanyRepository supplierCompanyRepository;

    @Autowired
    private UserRepository userRepository;

    // Implementar CRUD


    private SupplierCompany getSupplierCompanyById(Long id) {
        return supplierCompanyRepository.findById(id)
                .orElseThrow(() -> new NullPointerException("SupplierCompany not found with id: " + id));
    }

    private User getLastUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NullPointerException("User not found with id: " + id));
    }
}
