package com.codenine.managementservice.config;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.codenine.managementservice.dto.purchaseOrder.EmailStatus;
import com.codenine.managementservice.dto.purchaseOrder.Status;
import com.codenine.managementservice.dto.user.Role;
import com.codenine.managementservice.entity.*;
import com.codenine.managementservice.repository.*;
import com.codenine.managementservice.utils.NormalizeEmail;

@Component
@Profile("dev") // Só executa no profile dev
public class DataLoader implements CommandLineRunner {

  private User _user_adm;

  private List<Section> sections;

  private List<String> itemExcelFiles =
      List.of("src/main/resources/almoxarifado.xlsx", "src/main/resources/farmacia.xlsx");

  @Autowired private UserRepository userRepository;

  @Autowired private SectionRepository sectionRepository;

  @Autowired private ItemRepository itemRepository;

  @Autowired private ItemTypeRepository itemTypeRepository;

  @Autowired private SupplierCompanyRepository supplierCompanyRepository;

  @Autowired private OrderRepository orderRepository;

  @Autowired private OrderItemRepositorio orderItemRepository;

  @Autowired private PurchaseOrderRepository purchaseOrderRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private ExcelItemImporter excelItemImporter;

  private final Random random = new Random();

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    if (itemRepository.count() > 0) {
      System.out.println("Dados já existem, pulando inserção...");
      return;
    }

    System.out.println("Inserindo dados de teste...");

    // Criar sections
    List<Section> sections = createSections();
    System.out.println("Sections criadas: " + sections.size());

    createTypeItemsFromExcel();
    System.out.println("ItemTypes criados com sucesso.");

    // Criar suppliers
    List<SupplierCompany> suppliers = createSuppliers();
    System.out.println("Suppliers criados com sucesso.");

    createItemsFromExcel();
    System.out.println("Items criados com sucesso.");
    // Criar users
    List<User> users = createUsers(sections);
    System.out.println("Users criados com sucesso.");

    // Criar orders e purchase orders (janeiro 2023 até hoje)
    createOrdersAndPurchaseOrders(users, suppliers, sections);
    System.out.println("Orders e PurchaseOrders criados com sucesso.");

    System.out.println("Inserção de dados concluída!");
  }

  private List<Section> createSections() {
    List<Section> sections = new ArrayList<>();

    Section almoxarifado = new Section();
    almoxarifado.setTitle("Almoxarifado");
    almoxarifado.setIsActive(true);
    almoxarifado.setSectionType(SectionType.STORAGE);
    almoxarifado.setCreatedAt(LocalDateTime.now());
    almoxarifado.setLastUpdate(LocalDateTime.now());

    Section farmacia = new Section();
    farmacia.setTitle("Farmácia");
    farmacia.setIsActive(true);
    farmacia.setSectionType(SectionType.STORAGE);

    sections.add(almoxarifado);
    sections.add(farmacia);
    sectionRepository.saveAll(sections);

    User adminUser = new User();
    adminUser.setName("Administrador CODE NINE");
    adminUser.setEmail("codenine@email.com");
    adminUser.setPassword(passwordEncoder.encode("codenine123"));
    adminUser.setRole(Role.ADMIN);
    adminUser.setSections(sections);

    userRepository.save(adminUser);

    almoxarifado.setLastUser(adminUser);
    almoxarifado.setLastUpdate(LocalDateTime.now());

    farmacia.setLastUser(adminUser);
    farmacia.setLastUpdate(LocalDateTime.now());
    this.sections = sections;
    this._user_adm = adminUser;

    sectionRepository.saveAll(sections);

    return sections;
  }

  private void createTypeItemsFromExcel() {
    try {
      for (Section section : this.sections) {
        String filePath =
            section.getTitle().equalsIgnoreCase("Almoxarifado")
                ? "src/main/resources/almoxarifado.xlsx"
                : "src/main/resources/farmacia.xlsx";
        excelItemImporter.importItemTypesFromExcel(filePath, section.getId(), this._user_adm);
      }
      System.out.println("Itens importados do Excel com sucesso.");
    } catch (Exception e) {
      System.err.println("Erro ao importar itens do Excel: " + e.getMessage());
    }
  }

  private void createItemsFromExcel() {
    try {
      for (Section section : this.sections) {
        String filePath =
            section.getTitle().equalsIgnoreCase("Almoxarifado")
                ? "src/main/resources/almoxarifado.xlsx"
                : "src/main/resources/farmacia.xlsx";
        excelItemImporter.importItemsExcel(filePath, section.getId(), this._user_adm);
      }
      System.out.println("Itens importados do Excel com sucesso.");
    } catch (Exception e) {
      System.err.println("Erro ao importar itens do Excel: " + e.getMessage());
    }
  }

  private List<SupplierCompany> createSuppliers() {
    List<SupplierCompany> suppliers = new ArrayList<>();
    String[] supplierNames = {
      "Indústria Militar Brasileira",
      "Fábrica de Munições Caçapava",
      "Hospital Militar Regional",
      "Oficina de Manutenção Militar"
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
      supplier.setLastUser(this._user_adm);
      suppliers.add(supplier);
    }

    return supplierCompanyRepository.saveAll(suppliers);
  }

  private List<User> createUsers(List<Section> sections) {
    List<User> users = new ArrayList<>();
    String[] names = {
      "Capitão Silva", "Sargento Souza", "Tenente Lima", "Soldado Pereira", "Major Costa",
      "Coronel Ramos", "Sargento Oliveira", "Soldado Santos", "Tenente Braga", "Capitão Almeida"
    };

    for (int i = 0; i < names.length; i++) {
      User user = new User();
      user.setName(names[i]);
      String email = names[i].toLowerCase().replace(" ", ".") + "@exercito.mil.br";
      user.setEmail(NormalizeEmail.normalize(email));
      user.setPassword(passwordEncoder.encode("senha" + (i + 1)));
      if (names[i].contains("Capitão")) {
        user.setRole(Role.ADMIN);
      } else if (names[i].contains("Tenente") || names[i].contains("Major")) {
        user.setRole(Role.MANAGER);
      } else {
        user.setRole(Role.ASSISTANT);
      }

      List<Section> userSections = new ArrayList<>();
      userSections.add(sections.get(i % sections.size()));
      user.setSections(userSections);

      users.add(user);
    }

    return userRepository.saveAll(users);
  }

  private void createOrdersAndPurchaseOrders(
      List<User> users, List<SupplierCompany> suppliers, List<Section> sections) {
    LocalDateTime startDate = LocalDateTime.of(2023, 1, 1, 8, 0);
    LocalDateTime currentDate = LocalDateTime.now();
    
    List<Item> allItems = itemRepository.findAll();
    if (allItems.isEmpty()) {
      System.out.println("Nenhum item encontrado para criar pedidos.");
      return;
    }

    String[] statusOptions = {"PENDING", "APPROVED", "IN_TRANSIT", "DELIVERED", "CANCELLED"};
    int orderCounter = 1;

    // Percorrer meses de janeiro 2023 até o mês atual
    LocalDateTime monthStart = startDate;
    while (monthStart.isBefore(currentDate) || monthStart.isEqual(currentDate)) {
      int year = monthStart.getYear();
      int month = monthStart.getMonthValue();
      int yearMonth = year * 100 + month;

      // Criar 30 pedidos por mês
      for (int i = 0; i < 30; i++) {
        // Distribuir os pedidos ao longo do mês
        int dayOfMonth = (i % 28) + 1; // Evitar dias > 28 para simplificar
        int hour = 8 + (i % 10); // Horário entre 8h e 17h
        LocalDateTime orderDate = LocalDateTime.of(year, month, dayOfMonth, hour, 0);

        Section section = sections.get(random.nextInt(sections.size()));
        
        User creator = users.get(random.nextInt(users.size()));
        
        Order order = new Order();
        order.setOrderNumber(String.format("ORD-%04d-%05d", year, orderCounter++));
        order.setCreatedAt(orderDate);
        order.setLastUpdate(orderDate);
        order.setWithdrawDay(orderDate.plusDays(random.nextInt(30) + 1));
        order.setExpireAt(orderDate.plusDays(random.nextInt(60) + 30));
        order.setStatus(statusOptions[random.nextInt(statusOptions.length)]);
        order.setCreatedBy(creator);
        order.setLastUser(creator);
        order.setSection(section);

        // Criar OrderItems
        int itemCount = 2 + random.nextInt(7);
        List<OrderItem> orderItems = new ArrayList<>();
        List<Item> selectedItems = new ArrayList<>();
        
        // Filtrar itens pela seção (usando itemType)
        List<Item> sectionItems = allItems.stream()
            .filter(item -> item.getItemType() != null 
                && item.getItemType().getSection() != null 
                && item.getItemType().getSection().getId().equals(section.getId()))
            .toList();
        
        if (sectionItems.isEmpty()) {
          sectionItems = allItems;
        }

        for (int j = 0; j < itemCount && j < sectionItems.size(); j++) {
          Item item = sectionItems.get(random.nextInt(sectionItems.size()));
          
          if (selectedItems.contains(item)) {
            continue;
          }
          selectedItems.add(item);

          OrderItem orderItem = new OrderItem();
          orderItem.setItem(item);
          orderItem.setQuantity(1 + random.nextInt(50));
          orderItem.setLastUser(creator);
          orderItem.setOrder(order);
          orderItems.add(orderItem);

        }

        if (orderItems.isEmpty()) {
          continue;
        }

        order.setOrderItems(orderItems);
        
        Order savedOrder = orderRepository.save(order);

        if (random.nextDouble() < 0.8) {
          SupplierCompany supplier = suppliers.get(random.nextInt(suppliers.size()));
          
          PurchaseOrder purchaseOrder = new PurchaseOrder();
          purchaseOrder.setOrder(savedOrder);
          purchaseOrder.setSupplierCompany(supplier);
          purchaseOrder.setIssuingBody("Exército Brasileiro - " + section.getTitle());
          purchaseOrder.setCommitmentNoteNumber(String.format("NC-%04d-%05d", year, random.nextInt(99999)));
          purchaseOrder.setYear(year);
          purchaseOrder.setProcessNumber(String.format("PROC-%04d/%05d", year, random.nextInt(99999)));
          
          // Verificar se é quantidade ou valor
          float totalValue = orderItems.stream()
              .mapToInt(OrderItem::getQuantity)
              .sum() * (50f + random.nextFloat() * 450f);
          purchaseOrder.setTotalValue(totalValue);
          
          purchaseOrder.setIssueDate(orderDate);
          purchaseOrder.setCreatedAt(orderDate);
          purchaseOrder.setLastUpdate(orderDate);
          
          if (orderDate.isBefore(currentDate.minusMonths(3))) {
            purchaseOrder.setStatus(Status.DELIVERY);
            purchaseOrder.setEmailStatus(EmailStatus.SENT);
          } else if (orderDate.isBefore(currentDate.minusMonths(1))) {
            purchaseOrder.setStatus(random.nextBoolean() ? Status.DELIVERY : Status.PENDING_DELIVERY);
            purchaseOrder.setEmailStatus(EmailStatus.SENT);
          } else {
            purchaseOrder.setStatus(Status.PENDING_DELIVERY);
            purchaseOrder.setEmailStatus(random.nextBoolean() ? EmailStatus.SENT : EmailStatus.NOT_SENT);
          }
          
          purchaseOrder.setCreatedBy(creator);
          purchaseOrder.setLastUser(creator);
          purchaseOrder.setSender(creator);
          
          purchaseOrderRepository.save(purchaseOrder);
        }
      }

      monthStart = monthStart.plusMonths(1);
    }

    System.out.println("Total de pedidos criados: " + (orderCounter - 1));
  }
}
