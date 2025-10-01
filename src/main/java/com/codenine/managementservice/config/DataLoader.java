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

import com.codenine.managementservice.dto.user.Role;
import com.codenine.managementservice.entity.*;
import com.codenine.managementservice.repository.*;
import com.codenine.managementservice.utils.NormalizeEmail;

@Component
@Profile("dev") // Só executa no profile dev
public class DataLoader implements CommandLineRunner {

  private User _user_adm;

  private List<Section> sections;

  private List<String> itemExcelFiles = List.of("src/main/resources/almoxarifado.xlsx",
      "src/main/resources/farmacia.xlsx");

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private SectionRepository sectionRepository;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private ItemTypeRepository itemTypeRepository;

  @Autowired
  private SupplierCompanyRepository supplierCompanyRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private ExcelItemImporter excelItemImporter;

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

    System.out.println("Inserção de dados concluída!");
  }

  private List<Section> createSections() {
    List<Section> sections = new ArrayList<>();

    Section almoxarifado = new Section();
    almoxarifado.setTitle("Almoxarifado");
    almoxarifado.setIsActive(true);
    almoxarifado.setCreatedAt(LocalDateTime.now());
    almoxarifado.setLastUpdate(LocalDateTime.now());

    Section farmacia = new Section();
    farmacia.setTitle("Farmácia");
    farmacia.setIsActive(true);

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
        String filePath = section.getTitle().equalsIgnoreCase("Almoxarifado")
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
        String filePath = section.getTitle().equalsIgnoreCase("Almoxarifado")
            ? "src/main/resources/almoxarifado.xlsx"
            : "src/main/resources/farmacia.xlsx";
        excelItemImporter.importItemsExcel(
            filePath, section.getId(), this._user_adm);
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

    String[] phones = { "(11) 3456-7890", "(12) 3456-7891", "(13) 3456-7892", "(14) 3456-7893" };

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
}
