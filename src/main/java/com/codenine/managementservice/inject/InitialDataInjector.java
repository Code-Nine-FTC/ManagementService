package com.codenine.managementservice.inject;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.codenine.managementservice.repository.ItemRepository;

@Component
@Profile("dev")
public class InitialDataInjector implements CommandLineRunner {

  private final ItemRepository itemRepository;
  private final SectionInitializer sectionInitializer;
  private final ExcelItemBootstrap excelItemBootstrap;
  private final SupplierInitializer supplierInitializer;
  private final UserInitializer userInitializer;
  private final OrderPurchaseOrderGenerator orderPurchaseOrderGenerator;

  public InitialDataInjector(
      ItemRepository itemRepository,
      SectionInitializer sectionInitializer,
      ExcelItemBootstrap excelItemBootstrap,
      SupplierInitializer supplierInitializer,
      UserInitializer userInitializer,
      OrderPurchaseOrderGenerator orderPurchaseOrderGenerator) {
    this.itemRepository = itemRepository;
    this.sectionInitializer = sectionInitializer;
    this.excelItemBootstrap = excelItemBootstrap;
    this.supplierInitializer = supplierInitializer;
    this.userInitializer = userInitializer;
    this.orderPurchaseOrderGenerator = orderPurchaseOrderGenerator;
  }

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    if (itemRepository.count() > 0) {
      System.out.println("Dados já existem, pulando inserção inicial...");
      return;
    }
    System.out.println("Iniciando carga modular de dados de desenvolvimento...");
    var sectionResult = sectionInitializer.initializeSectionsAndAdmin();
    var sections = sectionResult.sections();
    var adminUser = sectionResult.adminUser();
    System.out.println("Sections e admin criados.");
    excelItemBootstrap.importTypesAndItems(sections, adminUser);
    System.out.println("Tipos e itens importados do Excel.");
    var suppliers = supplierInitializer.initialize(adminUser);
    System.out.println("Suppliers criados.");
    var users = userInitializer.initialize(sections);
    System.out.println("Users criados.");
    orderPurchaseOrderGenerator.generate(users, suppliers, sections);
    System.out.println("Orders e PurchaseOrders gerados.");
    System.out.println("Carga inicial concluída.");
  }
}
