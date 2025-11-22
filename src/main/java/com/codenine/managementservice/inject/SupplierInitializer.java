package com.codenine.managementservice.inject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.codenine.managementservice.entity.SupplierCompany;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.SupplierCompanyRepository;

@Component
public class SupplierInitializer {
  private final SupplierCompanyRepository supplierCompanyRepository;

  public SupplierInitializer(SupplierCompanyRepository supplierCompanyRepository) {
    this.supplierCompanyRepository = supplierCompanyRepository;
  }

  public List<SupplierCompany> initialize(User adminUser) {
    List<SupplierCompany> suppliers = new ArrayList<>();
    String[] supplierNames = {
      "Indústria Militar Brasileira", "Fábrica de Munições Caçapava", "Hospital Militar Regional", "Oficina de Manutenção Militar"
    };

    String[] cnpjs = {
      "11.222.333/0001-44", "22.333.444/0001-55", "33.444.555/0001-66", "44.555.666/0001-77"
    };

    String[] emails = {
      "contato@imb.mil.br", "contato@fmc.mil.br", "contato@hmr.mil.br", "contato@omm.mil.br"
    };

    String[] phones = {"(11) 3456-7890", "(12) 3456-7891", "(13) 3456-7892", "(14) 3456-7893"};

    for (int i = 0; i < supplierNames.length; i++) {
      SupplierCompany supplier = new SupplierCompany();
      supplier.setName(supplierNames[i]);
      supplier.setCnpj(cnpjs[i]);
      supplier.setEmail(emails[i]);
      supplier.setPhoneNumber(phones[i]);
      supplier.setIsActive(true);
      supplier.setLastUpdate(LocalDateTime.now());
      supplier.setLastUser(adminUser);
      suppliers.add(supplier);
    }
    return supplierCompanyRepository.saveAll(suppliers);
  }
}
