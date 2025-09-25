package com.codenine.managementservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.dto.supplier.SupplierCompanyRequest;
import com.codenine.managementservice.dto.supplier.SupplierCompanyResponse;
import com.codenine.managementservice.entity.SupplierCompany;
import com.codenine.managementservice.repository.ItemRepository;
import com.codenine.managementservice.repository.SupplierCompanyRepository;
import com.codenine.managementservice.repository.UserRepository;

@Service
public class SupplierCompanyService {
  @Autowired private com.codenine.managementservice.repository.OrderRepository orderRepository;

  public SupplierCompanyResponse toResponse(SupplierCompany entity) {
    if (entity == null) return null;
    return new SupplierCompanyResponse(
        entity.getId(),
        entity.getName(),
        entity.getEmail(),
        entity.getPhoneNumber(),
        entity.getCnpj(),
        entity.getIsActive(),
        entity.getRating(),
        entity.getLastUpdate(),
        entity.getLastUser() != null ? entity.getLastUser().getName() : null,
        entity.getItems() != null
            ? entity.getItems().stream().map(item -> item.getId()).toList()
            : null,
        entity.getOrders() != null
            ? entity.getOrders().stream().map(order -> order.getId()).toList()
            : null);
  }

  @Autowired private UserRepository userRepository;
  @Autowired private ItemRepository itemRepository;

  public SupplierCompany toEntity(SupplierCompanyRequest req) {
    if (req == null) return null;
    SupplierCompany entity = new SupplierCompany();
    entity.setName(req.name());
    entity.setEmail(req.email());
    entity.setPhoneNumber(req.phoneNumber());
    entity.setCnpj(req.cnpj());
    entity.setIsActive(req.isActive());
    entity.setRating(req.rating());
    entity.setLastUpdate(java.time.LocalDateTime.now());
    // lastUser
    if (req.lastUserId() != null) {
      entity.setLastUser(userRepository.findById(req.lastUserId()).orElse(null));
    }
    // items
    if (req.itemIds() != null && !req.itemIds().isEmpty()) {
      entity.setItems(itemRepository.findAllById(req.itemIds()));
    }
    // orders
    if (req.orderIds() != null && !req.orderIds().isEmpty()) {
      entity.setOrders(orderRepository.findAllById(req.orderIds()));
    }
    return entity;
  }

  @Autowired private SupplierCompanyRepository supplierCompanyRepository;

  // CRUD Methods

  public SupplierCompanyResponse createSupplierCompany(SupplierCompanyRequest req) {
    SupplierCompany entity = toEntity(req);
    SupplierCompany saved = supplierCompanyRepository.save(entity);
    return toResponse(saved);
  }

  public SupplierCompanyResponse updateSupplierCompany(Long id, SupplierCompanyRequest req) {
    SupplierCompany entity = toEntity(req);
    entity.setId(id);
    SupplierCompany updated = supplierCompanyRepository.save(entity);
    return toResponse(updated);
  }

  public SupplierCompanyResponse getSupplierCompany(Long id) {
    return toResponse(getSupplierCompanyById(id));
  }

  public java.util.List<SupplierCompanyResponse> getAllSupplierCompanies() {
    return supplierCompanyRepository.findAllSupplierCompanyResponses(null, null, null);
  }

  public java.util.List<SupplierCompanyResponse> getSupplierCompaniesWithFilters(
      Long supplierId, Boolean isActive, Long lastUserId) {
    return supplierCompanyRepository.findAllSupplierCompanyResponses(
        supplierId, isActive, lastUserId);
  }

  public void deleteSupplierCompany(Long id) {
    SupplierCompany existing = getSupplierCompanyById(id);
    supplierCompanyRepository.delete(existing);
  }

  private SupplierCompany getSupplierCompanyById(Long id) {
    return supplierCompanyRepository
        .findById(id)
        .orElseThrow(() -> new NullPointerException("SupplierCompany not found with id: " + id));
  }
}
