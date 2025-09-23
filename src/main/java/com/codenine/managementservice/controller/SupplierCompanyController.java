package com.codenine.managementservice.controller;

import com.codenine.managementservice.dto.supplier.SupplierCompanyRequest;
import com.codenine.managementservice.dto.supplier.SupplierCompanyResponse;
import com.codenine.managementservice.service.SupplierCompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/suppliers")
public class SupplierCompanyController {
    @Autowired
    private SupplierCompanyService supplierCompanyService;

    @GetMapping
    public ResponseEntity<List<SupplierCompanyResponse>> getAllSuppliers() {
        return ResponseEntity.ok(supplierCompanyService.getAllSupplierCompanies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierCompanyResponse> getSupplier(@PathVariable Long id) {
        try {
            SupplierCompanyResponse response = supplierCompanyService.getSupplierCompany(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<SupplierCompanyResponse> createSupplier(@RequestBody SupplierCompanyRequest supplierCompanyRequest) {
        try {
            SupplierCompanyResponse created = supplierCompanyService.createSupplierCompany(supplierCompanyRequest);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<SupplierCompanyResponse> updateSupplier(@PathVariable Long id, @RequestBody SupplierCompanyRequest supplierCompanyRequest) {
        try {
            SupplierCompanyResponse updated = supplierCompanyService.updateSupplierCompany(id, supplierCompanyRequest);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        try {
            supplierCompanyService.deleteSupplierCompany(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
