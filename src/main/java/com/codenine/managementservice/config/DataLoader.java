package com.codenine.managementservice.config;

import com.codenine.managementservice.entity.*;
import com.codenine.managementservice.repository.*;
import com.codenine.managementservice.dto.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Profile("dev") // Só executa no profile dev
public class DataLoader implements CommandLineRunner {

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
        
        // Criar item types
        List<ItemType> itemTypes = createItemTypes();
        System.out.println("ItemTypes criados: " + itemTypes.size());
        
        // Criar suppliers
        List<SupplierCompany> suppliers = createSuppliers();
        System.out.println("Suppliers criados: " + suppliers.size());
        
        // Criar users
        List<User> users = createUsers(sections);
        System.out.println("Users criados: " + users.size());
        
        // Criar items em lotes (muito mais eficiente)
        createItemsInBatches(users, sections, itemTypes, suppliers);
        
        System.out.println("Inserção de dados concluída!");
    }

    private List<Section> createSections() {
        List<Section> sections = new ArrayList<>();
        
        Section almoxarifado = new Section();
        almoxarifado.setTitle("Almoxarifado");
        almoxarifado.setIsActive(true);
        
        Section farmacia = new Section();
        farmacia.setTitle("Farmácia");
        farmacia.setIsActive(true);
        
        sections.add(almoxarifado);
        sections.add(farmacia);
        
        return sectionRepository.saveAll(sections);
    }

    private List<ItemType> createItemTypes() {
        List<ItemType> itemTypes = new ArrayList<>();
        
        // Tipos militares
        for (int i = 1; i <= 50; i++) {
            ItemType itemType = new ItemType();
            itemType.setName("Tipo Militar " + i);
            itemTypes.add(itemType);
        }
        
        // Tipos farmácia
        for (int i = 1; i <= 50; i++) {
            ItemType itemType = new ItemType();
            itemType.setName("Tipo Farmácia " + i);
            itemTypes.add(itemType);
        }
        
        return itemTypeRepository.saveAll(itemTypes);
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
            "11.222.333/0001-44",
            "22.333.444/0001-55", 
            "33.444.555/0001-66",
            "44.555.666/0001-77"
        };
        
        String[] urls = {
            "https://www.imb.mil.br",
            "https://www.fmc.mil.br",
            "https://www.hmr.mil.br", 
            "https://www.omm.mil.br"
        };
        
        String[] emails = {
            "contato@imb.mil.br",
            "contato@fmc.mil.br",
            "contato@hmr.mil.br",
            "contato@omm.mil.br"
        };
        
        String[] phones = {
            "(11) 3456-7890",
            "(12) 3456-7891",
            "(13) 3456-7892",
            "(14) 3456-7893"
        };

        for (int i = 0; i < supplierNames.length; i++) {
            SupplierCompany supplier = new SupplierCompany();
            supplier.setName(supplierNames[i]);
            supplier.setCnpj(cnpjs[i]);
            supplier.setUrl(urls[i]);
            supplier.setEmail(emails[i]);
            supplier.setPhoneNumber(phones[i]);
            supplier.setIsActive(true);
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
            user.setEmail(names[i].toLowerCase().replace(" ", ".") + "@exercito.mil.br");
            user.setPassword("{noop}senha" + (i + 1));
            
            if (names[i].contains("Capitão")) {
                user.setRole(Role.ADMIN);
            } else if (names[i].contains("Tenente") || names[i].contains("Major")) {
                user.setRole(Role.MANAGER);
            } else {
                user.setRole(Role.ASSISTANT);
            }
            
            user.setIsActive(true);
            user.setSection(sections.get(i % sections.size()));
            users.add(user);
        }

        return userRepository.saveAll(users);
    }

    private void createItemsInBatches(List<User> users, List<Section> sections, List<ItemType> itemTypes, List<SupplierCompany> suppliers) {
        String[] almoxarifadoItems = {
            "Calça Camuflada", "Radio HT Motorola", "Fuzil IA2", "Cartucho 5.56mm",
            "Chave Inglesa", "Barraca Militar", "Prancheta de Ordem", "Capacete Balístico"
        };
        
        String[] farmaciaItems = {
            "Kit Primeiros Socorros", "Termômetro Digital", "Álcool Gel", "Luvas Cirúrgicas",
            "Máscara Descartável", "Soro Fisiológico", "Esparadrapo", "Medicamento Analgésico"
        };

        String[] cores = {"Verde", "Preto", "Cinza", "Azul"};
        String[] tamanhos = {"P", "M", "G", "GG"};

        // Inserir em lotes de 1000 para performance
        int batchSize = 1000;
        List<Item> batch = new ArrayList<>();

        // Items do Almoxarifado
        Section almoxarifado = sections.get(0);
        for (int i = 1; i <= 7000; i++) {
            Item item = new Item();
            String nameBase = almoxarifadoItems[random.nextInt(almoxarifadoItems.length)];
            String cor = cores[random.nextInt(cores.length)];
            String tamanho = tamanhos[random.nextInt(tamanhos.length)];
            
            item.setName(nameBase + " " + cor + " " + tamanho + " " + i);
            item.setMeasure("unidade");
            item.setExpireDate(LocalDateTime.of(2026, 12, 31, 23, 59, 59));
            item.setMinimumStock(random.nextInt(20) + 1);
            int maxStock = item.getMinimumStock() + random.nextInt(200) + 10;
            item.setCurrentStock(random.nextInt(maxStock - item.getMinimumStock()) + item.getMinimumStock());
            item.setQrCode("QR" + String.format("%05d", i));
            item.setIsActive(true);
            item.setLastUser(users.get(random.nextInt(users.size())));
            item.setSection(almoxarifado);
            item.setItemType(itemTypes.get((i - 1) % 50)); // Tipos militares (0-49)
            item.setSupplier(suppliers.get(random.nextInt(suppliers.size())));

            batch.add(item);

            if (batch.size() == batchSize || i == 7000) {
                itemRepository.saveAll(batch);
                batch.clear();
                System.out.println("Inseridos " + i + " items do almoxarifado...");
            }
        }

        // Items da Farmácia
        Section farmacia = sections.get(1);
        for (int i = 7001; i <= 10000; i++) {
            Item item = new Item();
            String nameBase = farmaciaItems[random.nextInt(farmaciaItems.length)];
            String lote = "Lote " + String.format("%04d", i);
            
            item.setName(nameBase + " " + lote);
            item.setMeasure("unidade");
            item.setExpireDate(LocalDateTime.of(2026, 12, 31, 23, 59, 59));
            item.setMinimumStock(random.nextInt(20) + 1);
            int maxStock = item.getMinimumStock() + random.nextInt(200) + 10;
            item.setCurrentStock(random.nextInt(maxStock - item.getMinimumStock()) + item.getMinimumStock());
            item.setQrCode("QR" + String.format("%05d", i));
            item.setIsActive(true);
            item.setLastUser(users.get(random.nextInt(users.size())));
            item.setSection(farmacia);
            item.setItemType(itemTypes.get(50 + ((i - 7001) % 50))); // Tipos farmácia (50-99)
            item.setSupplier(suppliers.get(random.nextInt(suppliers.size())));

            batch.add(item);

            if (batch.size() == batchSize || i == 10000) {
                itemRepository.saveAll(batch);
                batch.clear();
                System.out.println("Inseridos " + (i - 7000) + " items da farmácia...");
            }
        }
    }
}