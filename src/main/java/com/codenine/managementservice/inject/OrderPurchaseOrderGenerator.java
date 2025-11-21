package com.codenine.managementservice.inject;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.codenine.managementservice.dto.purchaseOrder.EmailStatus;
import com.codenine.managementservice.dto.purchaseOrder.Status;
import com.codenine.managementservice.dto.order.OrderStatus;
import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.entity.Order;
import com.codenine.managementservice.entity.OrderItem;
import com.codenine.managementservice.entity.PurchaseOrder;
import com.codenine.managementservice.entity.Section;
import com.codenine.managementservice.entity.SupplierCompany;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.ItemRepository;
import com.codenine.managementservice.repository.OrderItemRepositorio;
import com.codenine.managementservice.repository.OrderRepository;
import com.codenine.managementservice.repository.PurchaseOrderRepository;
import com.github.javafaker.Faker;

@Component
public class OrderPurchaseOrderGenerator {

  private final ItemRepository itemRepository;
  private final OrderRepository orderRepository;
  private final OrderItemRepositorio orderItemRepositorio;
  private final PurchaseOrderRepository purchaseOrderRepository;
  private final Faker faker = new Faker();
  private final Random random = new Random();

  public OrderPurchaseOrderGenerator(
      ItemRepository itemRepository,
      OrderRepository orderRepository,
      OrderItemRepositorio orderItemRepositorio,
      PurchaseOrderRepository purchaseOrderRepository) {
    this.itemRepository = itemRepository;
    this.orderRepository = orderRepository;
    this.orderItemRepositorio = orderItemRepositorio;
    this.purchaseOrderRepository = purchaseOrderRepository;
  }

  @Transactional
  public void generate(List<User> users, List<SupplierCompany> suppliers, List<Section> sections) {
    LocalDateTime startDate = LocalDateTime.of(2023, 1, 1, 8, 0);
    LocalDateTime currentDate = LocalDateTime.now();

    List<Item> allItems = itemRepository.findAll();
    if (allItems.isEmpty()) {
      System.out.println("Nenhum item encontrado para criar pedidos.");
      return;
    }

    int orderCounter = 1;
    long purchaseOrderCounter = 1; // garante unicidade dos números de OC

    LocalDateTime monthStart = startDate;
    while (monthStart.isBefore(currentDate) || monthStart.isEqual(currentDate)) {
      int year = monthStart.getYear();
      int month = monthStart.getMonthValue();

      for (int i = 0; i < 30; i++) {
        int dayOfMonth = (i % 28) + 1; // simplificação
        int hour = 8 + (i % 10);
        LocalDateTime orderDate = LocalDateTime.of(year, month, dayOfMonth, hour, 0);

        Section section = sections.get(random.nextInt(sections.size()));
        User creator = users.get(random.nextInt(users.size()));

        Order order = new Order();
        order.setOrderNumber(String.format("ORD-%04d-%05d", year, orderCounter++));
        order.setCreatedAt(orderDate);
        order.setLastUpdate(orderDate);
        order.setWithdrawDay(orderDate.plusDays(random.nextInt(30) + 1));
        order.setExpireAt(orderDate.plusDays(random.nextInt(60) + 30));
        // Lógica temporal para status: mais antigos tendem a COMPLETED
        String computedStatus = computeOrderStatus(orderDate, currentDate);
        order.setStatus(computedStatus);
        order.setCreatedBy(creator);
        order.setLastUser(creator);
        order.setSection(section);

        int itemCount = 2 + random.nextInt(7);
        List<OrderItem> orderItems = new ArrayList<>();
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
          boolean alreadyUsed = orderItems.stream().anyMatch(oi -> oi.getItem().getId().equals(item.getId()));
          if (alreadyUsed) {
            continue;
          }
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
        orderItemRepositorio.saveAll(orderItems);

        if (random.nextDouble() < 0.8) {
          SupplierCompany supplier = suppliers.get(random.nextInt(suppliers.size()));
          PurchaseOrder purchaseOrder = buildPurchaseOrder(savedOrder, supplier, section, creator, currentDate, purchaseOrderCounter++);
          enrichOptionalFields(purchaseOrder); // novos campos via reflection + Faker
          purchaseOrderRepository.save(purchaseOrder);
        }
      }
      monthStart = monthStart.plusMonths(1);
    }
    System.out.println("Geração concluída.");
  }

  private String computeOrderStatus(LocalDateTime orderDate, LocalDateTime now) {
    // Antigos (> 5 meses) quase sempre COMPLETED (90%) ou CANCELLED (10%)
    if (orderDate.isBefore(now.minusMonths(5))) {
      return random.nextDouble() < 0.9 ? OrderStatus.COMPLETED.name() : OrderStatus.CANCELLED.name();
    }
    // Médio prazo (2-5 meses) distribuído entre PROCESSING (50%), APPROVED (30%), COMPLETED (15%), CANCELLED (5%)
    if (orderDate.isBefore(now.minusMonths(2))) {
      double p = random.nextDouble();
      if (p < 0.50) return OrderStatus.PROCESSING.name();
      if (p < 0.80) return OrderStatus.APPROVED.name();
      if (p < 0.95) return OrderStatus.COMPLETED.name();
      return OrderStatus.CANCELLED.name();
    }
    // Recentes (< 2 meses) PENDING (60%), APPROVED (25%), PROCESSING (10%), CANCELLED (5%)
    double p = random.nextDouble();
    if (p < 0.60) return OrderStatus.PENDING.name();
    if (p < 0.85) return OrderStatus.APPROVED.name();
    if (p < 0.95) return OrderStatus.PROCESSING.name();
    return OrderStatus.CANCELLED.name();
  }

  private PurchaseOrder buildPurchaseOrder(
      Order order,
      SupplierCompany supplier,
      Section section,
      User creator,
      LocalDateTime currentDate,
      long purchaseOrderSequence) {
    LocalDateTime orderDate = order.getCreatedAt();
    PurchaseOrder po = new PurchaseOrder();
    // número determinístico e único baseado em sequência
    String poNumber = String.format("OC-%04d-%05d", orderDate.getYear(), purchaseOrderSequence);
    po.setPurchaseOrderNumber(poNumber);
    po.setSupplierCompany(supplier);
    po.setIssuingBody("Exército Brasileiro - " + section.getTitle());
    po.setCommitmentNoteNumber(String.format("NC-%04d-%05d", orderDate.getYear(), random.nextInt(99999)));
    po.setYear(orderDate.getYear());
    po.setProcessNumber(String.format("PROC-%04d/%05d", orderDate.getYear(), random.nextInt(99999)));
    float totalValue = order.getOrderItems().stream().mapToInt(OrderItem::getQuantity).sum() * (50f + random.nextFloat() * 450f);
    po.setTotalValue(totalValue);
    po.setIssueDate(orderDate);
    po.setCreatedAt(orderDate);
    po.setLastUpdate(orderDate);
    if (orderDate.isBefore(currentDate.minusMonths(3))) {
      po.setStatus(Status.DELIVERY);
      po.setEmailStatus(EmailStatus.SENT);
    } else if (orderDate.isBefore(currentDate.minusMonths(1))) {
      po.setStatus(random.nextBoolean() ? Status.DELIVERY : Status.PENDING_DELIVERY);
      po.setEmailStatus(EmailStatus.SENT);
    } else {
      po.setStatus(Status.PENDING_DELIVERY);
      po.setEmailStatus(random.nextBoolean() ? EmailStatus.SENT : EmailStatus.NOT_SENT);
    }
    po.setCreatedBy(creator);
    po.setLastUser(creator);
    po.setSender(creator);
    return po;
  }

  private void enrichOptionalFields(PurchaseOrder po) {
    // Nomes potenciais de novos campos adicionados à entidade PurchaseOrder
    record ExtraField(String name, Supplier<Object> supplier) {}
    List<ExtraField> candidates = List.of(
        new ExtraField("contractNumber", () -> faker.number().digits(8)),
        new ExtraField("invoiceNumber", () -> "NF-" + faker.number().digits(6)),
        new ExtraField("paymentTerm", () -> faker.commerce().promotionCode()),
        new ExtraField("deliveryForecast", () -> LocalDateTime.now().plusDays(faker.number().numberBetween(5, 45))),
        new ExtraField("contactEmail", () -> faker.internet().emailAddress()),
        new ExtraField("contactName", () -> faker.name().fullName()),
        new ExtraField("remarks", () -> faker.lorem().sentence()),
        new ExtraField("modality", () -> faker.options().option("Pregão", "Dispensa", "Concorrência", "RDC"))
    );
    for (ExtraField ef : candidates) {
      try {
        Field f = po.getClass().getDeclaredField(ef.name());
        f.setAccessible(true);
        Object current = f.get(po);
        if (current == null) {
          f.set(po, ef.supplier().get());
        }
      } catch (NoSuchFieldException ignored) {
        // Campo não existe na versão atual da entidade, ignorar.
      } catch (IllegalAccessException e) {
        System.err.println("Falha ao atribuir campo extra: " + ef.name() + " -> " + e.getMessage());
      }
    }
  }
}
