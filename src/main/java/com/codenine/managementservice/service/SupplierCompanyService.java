package com.codenine.managementservice.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.dto.supplier.SupplierCompanyFilterCriteria;
import com.codenine.managementservice.dto.supplier.SupplierCompanyRequest;
import com.codenine.managementservice.dto.supplier.SupplierCompanyResponse;
import com.codenine.managementservice.entity.SupplierCompany;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.SupplierCompanyRepository;
import com.codenine.managementservice.utils.mapper.SupplierCompanyMapper;

@Service
public class SupplierCompanyService {

  @Autowired
  private SupplierCompanyRepository supplierCompanyRepository;

  public void createSupplierCompany(SupplierCompanyRequest request, User lastUser) {
    SupplierCompany entity = SupplierCompanyMapper.toEntity(request, lastUser);
    supplierCompanyRepository.save(entity);
  }

  public void updateSupplierCompany(Long id, SupplierCompanyRequest request, User lastUser) {
    getSupplierCompanyById(id);
    SupplierCompany entity = SupplierCompanyMapper.toUpdate(getSupplierCompanyById(id), request, lastUser);
    supplierCompanyRepository.save(entity);
  }

  public SupplierCompanyResponse getSupplierCompany(Long id) {
    return supplierCompanyRepository.findAllSupplierCompanyResponses(id, null).stream().findFirst()
        .orElseThrow(() -> new NullPointerException("SupplierCompany not found with id: " + id));
  }

  public List<SupplierCompanyResponse> getAllSupplierCompanies(SupplierCompanyFilterCriteria filter) {
    return supplierCompanyRepository.findAllSupplierCompanyResponses(filter.supplierId(), filter.isActive());
  }

  public void switchSupplierCompanyActive(Long id, User lastUser) {
    SupplierCompany entity = getSupplierCompanyById(id);
    entity.setIsActive(!entity.getIsActive());
    entity.setLastUpdate(LocalDateTime.now());
    entity.setLastUser(lastUser);
    supplierCompanyRepository.save(entity);
  }

  private SupplierCompany getSupplierCompanyById(Long id) {
    return supplierCompanyRepository
        .findById(id)
        .orElseThrow(() -> new NullPointerException("SupplierCompany not found with id: " + id));
  }
}
