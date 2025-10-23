package com.codenine.managementservice.service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.dto.notification.NotificationSeverity;
import com.codenine.managementservice.dto.notification.NotificationType;
import com.codenine.managementservice.dto.order.OrderFilterCriteria;
import com.codenine.managementservice.dto.order.OrderItemResponse;
import com.codenine.managementservice.dto.order.OrderRequest;
import com.codenine.managementservice.dto.order.OrderResponse;
import com.codenine.managementservice.dto.order.OrderStatus;
import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.entity.Order;
import com.codenine.managementservice.entity.Section;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.ItemRepository;
import com.codenine.managementservice.repository.OrderRepository;
import com.codenine.managementservice.repository.SectionRepository;
import com.codenine.managementservice.utils.mapper.OrderMapper;

import jakarta.persistence.EntityNotFoundException;

@Service
public class OrderService {

  @Autowired private OrderRepository orderRepository;

  @Autowired private ItemRepository itemRepository;

  @Autowired private SectionRepository sectionRepository;

  @Autowired private NotificationService notificationService;

  // SupplierCompany linkage removed

  public Long createOrder(OrderRequest request, User lastUser) {
    // Validar número do pedido
    if (request.orderNumber() == null || request.orderNumber().isBlank()) {
      throw new IllegalArgumentException("orderNumber é obrigatório");
    }
    if (orderRepository.existsByOrderNumber(request.orderNumber())) {
      // Usaremos IllegalStateException para diferenciar no controller e retornar 409
      throw new IllegalStateException("Número do pedido já existente");
    }
    
    // Notifications
    @SuppressWarnings("unused")
    NotificationService notificationServiceRef = this.notificationService;
  // Converter chaves String -> Long para itemQuantities
  Map<Long, Integer> itemQuantitiesLong =
    request.itemQuantities() != null
      ? request.itemQuantities().entrySet().stream()
        .collect(Collectors.toMap(e -> Long.parseLong(e.getKey()), Map.Entry::getValue))
      : java.util.Collections.emptyMap();

  List<Item> items = itemRepository.findAllById(itemQuantitiesLong.keySet());
  if (items.size() != itemQuantitiesLong.keySet().size())
      throw new IllegalArgumentException("Um ou mais IDs de item são inválidos.");
    // Resolve seção consumidora: prioridade para consumerSectionId; senão usa sectionId (compat). Sem fallback para seção do usuário.
    Section section = null;
    Long consumerSectionId = request.consumerSectionId() != null ? request.consumerSectionId() : request.sectionId();
    if (consumerSectionId == null) {
      throw new IllegalArgumentException("consumerSectionId é obrigatório para associar o consumo.");
    }
    section = sectionRepository.findById(consumerSectionId).orElseThrow(() -> new IllegalArgumentException("Seção consumidora inválida."));
    // Garantir que é uma seção consumidora
    if (section.getSectionType() != null && section.getSectionType() != com.codenine.managementservice.entity.SectionType.CONSUMER) {
      throw new IllegalArgumentException("A seção informada não é do tipo CONSUMER.");
    }
    Order order = OrderMapper.toEntity(request, itemQuantitiesLong, lastUser, items, section);
    // withdrawDay (LocalDate) opcional
    if (request.withdrawDay() != null) {
      LocalDate d = request.withdrawDay();
      order.setWithdrawDay(d.atStartOfDay());
    }
    Order saved = orderRepository.save(order);
    // Notificação de criação de pedido (mensagem sem fornecedor)
    if (notificationService != null) {
      notificationService.createNotification(
          NotificationType.ORDER_CREATED,
          "Novo pedido #" + saved.getId() + " criado",
          NotificationSeverity.INFO,
          null,
          saved,
          7776000L);
    }
    return saved.getId();
  }

  public void updateOrder(Long orderId, OrderRequest request, User lastUser) {
    Order order = getOrderById(orderId);
    // Não permitimos alteração do número do pedido neste momento
    // Se enviado e diferente, rejeita
    if (request.orderNumber() != null && !request.orderNumber().isBlank()
        && (order.getOrderNumber() == null || !order.getOrderNumber().equals(request.orderNumber()))) {
      throw new IllegalArgumentException("Alteração do número do pedido não é permitida.");
    }

    if (request.itemQuantities() != null && !request.itemQuantities().isEmpty()) {
      Map<Long, Integer> itemQuantitiesLong =
          request.itemQuantities().entrySet().stream()
              .collect(Collectors.toMap(e -> Long.parseLong(e.getKey()), Map.Entry::getValue));
      List<Item> items = itemRepository.findAllById(itemQuantitiesLong.keySet());
      if (items.size() != itemQuantitiesLong.keySet().size())
        throw new IllegalArgumentException("Um ou mais IDs de item são inválidos.");
      order = OrderMapper.toUpdate(order, itemQuantitiesLong, lastUser, items);
    }

    if (request.withdrawDay() != null) {
      LocalDate d = request.withdrawDay();
      order.setWithdrawDay(d.atStartOfDay());
      order.setLastUser(lastUser);
      order.setLastUpdate(LocalDateTime.now());
    }

    Long consumerSectionId = request.consumerSectionId() != null ? request.consumerSectionId() : request.sectionId();
    if (consumerSectionId != null) {
      Section section = sectionRepository.findById(consumerSectionId).orElseThrow(() -> new IllegalArgumentException("Seção consumidora inválida."));
      if (section.getSectionType() != null && section.getSectionType() != com.codenine.managementservice.entity.SectionType.CONSUMER) {
        throw new IllegalArgumentException("A seção informada não é do tipo CONSUMER.");
      }
      order.setSection(section);
      order.setLastUser(lastUser);
      order.setLastUpdate(LocalDateTime.now());
    }

    orderRepository.save(order);
  }

  public void cancelOrder(Long orderId, User lastUser) {
    Order order = getOrderById(orderId);
    order.setStatus(OrderStatus.CANCELLED.name());
    order.setLastUser(lastUser);
    order.setLastUpdate(LocalDateTime.now());
    orderRepository.save(order);
    
    notificationService.createNotification(
        NotificationType.ORDER_CANCELLED,
        "Pedido #" + orderId + " foi cancelado",
        NotificationSeverity.CRITICAL,
        null,
        order,
        7776000L);
  }

  public List<OrderResponse> getAllOrders(OrderFilterCriteria criteria) {
    return orderRepository.findAllOrderResponses(
        criteria.orderId() != null ? criteria.orderId() : null,
        criteria.status() != null ? criteria.status().name() : null,
        criteria.sectionId() != null ? criteria.sectionId() : null);
  }

  public List<OrderItemResponse> getOrderItemsByOrderId(Long orderId) {
    getOrderById(orderId);
    return orderRepository.findAllOrderItemResponsesByOrderId(orderId);
  }

  public OrderResponse getOrderResponseById(Long orderId) {
    return orderRepository.findAllOrderResponses(orderId, null, null).stream()
        .findFirst()
        .orElseThrow(
            () -> new EntityNotFoundException("Ordem com ID " + orderId + " não encontrada."));
  }

  public void approveOrder(Long orderId, User lastUser) {
    Order order = getOrderById(orderId);
    order.setStatus(OrderStatus.APPROVED.name());
    order.setLastUser(lastUser);
    order.setLastUpdate(LocalDateTime.now());
    orderRepository.save(order);
    
    notificationService.createNotification(
        NotificationType.ORDER_APPROVED,
        "Pedido #" + orderId + " foi aprovado",
        NotificationSeverity.APPROVED,
        null,
        order,
        7776000L);
  }

  public void processOrder(Long orderId, User lastUser) {
    Order order = getOrderById(orderId);
    order.setStatus(OrderStatus.PROCESSING.name());
    order.setLastUser(lastUser);
    order.setLastUpdate(LocalDateTime.now());
    orderRepository.save(order);
    
    notificationService.createNotification(
        NotificationType.ORDER_PROCESSING,
        "Pedido #" + orderId + " está sendo processado",
        NotificationSeverity.PROCESSING,
        null,
        order,
        7776000L);
  }

  public void completeOrder(Long orderId, User lastUser, LocalDateTime withdrawDay) {
    Order order = getOrderById(orderId);
    order.setStatus(OrderStatus.COMPLETED.name());
    order.setWithdrawDay(withdrawDay != null ? withdrawDay : LocalDateTime.now());
    order.setLastUser(lastUser);
    order.setLastUpdate(LocalDateTime.now());
    orderRepository.save(order);
    
    notificationService.createNotification(
        NotificationType.ORDER_COMPLETED,
        "Pedido #" + orderId + " foi concluído",
        NotificationSeverity.SUCCESS,
        null,
        order,
        7776000L); // 90 dias
  }

  private Order getOrderById(Long id) {
    return orderRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Ordem com ID " + id + " não encontrada."));
  }
}