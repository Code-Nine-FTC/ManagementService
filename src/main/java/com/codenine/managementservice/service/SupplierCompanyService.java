package com.codenine.managementservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.entity.SupplierCompany;
import com.codenine.managementservice.repository.SupplierCompanyRepository;

@Service
public class SupplierCompanyService {
  @Autowired private SupplierCompanyRepository supplierCompanyRepository;

  // Implementar CRUD

  private SupplierCompany getSupplierCompanyById(Long id) {
    return supplierCompanyRepository
        .findById(id)
        .orElseThrow(() -> new NullPointerException("SupplierCompany not found with id: " + id));
  }
}
