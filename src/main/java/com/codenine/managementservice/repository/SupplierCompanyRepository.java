package com.codenine.managementservice.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.codenine.managementservice.entity.SupplierCompany;

public interface SupplierCompanyRepository extends JpaRepository<SupplierCompany, Long> {
    
}
