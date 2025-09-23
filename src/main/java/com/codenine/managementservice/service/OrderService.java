package com.codenine.managementservice.service;

import com.codenine.managementservice.dto.order.OrderRequest;
import com.codenine.managementservice.dto.order.OrderResponse;
import com.codenine.managementservice.dto.user.UserRequest;
import com.codenine.managementservice.dto.item.ItemResponse;
import com.codenine.managementservice.dto.supplier.SupplierCompanyResponse;
import com.codenine.managementservice.entity.Order;
import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.entity.SupplierCompany;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.entity.Section;
import com.codenine.managementservice.repository.OrderRepository;
import com.codenine.managementservice.repository.ItemRepository;
import com.codenine.managementservice.repository.SupplierCompanyRepository;
import com.codenine.managementservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final SupplierCompanyRepository supplierRepository;
    private final UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    public OrderService(OrderRepository orderRepository, ItemRepository itemRepository, SupplierCompanyRepository supplierRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.supplierRepository = supplierRepository;
        this.userRepository = userRepository;
    }

    public Order createOrder(OrderRequest request) {
        List<Item> items = itemRepository.findAllById(request.itemIds());
        List<SupplierCompany> suppliers = supplierRepository.findAllById(request.supplierIds());

        if (items.size() != request.itemIds().size()) {
            throw new IllegalArgumentException("Um ou mais IDs de item são inválidos.");
        }
        if (suppliers.size() != request.supplierIds().size()) {
            throw new IllegalArgumentException("Um ou mais IDs de fornecedor são inválidos.");
        }

        Order order = new Order();
    order.setItems(items);
    order.setSuppliers(suppliers);
    order.setWithdrawDay(request.withdrawDay());
    order.setStatus(request.status() != null ? request.status() : "PENDENTE");

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + userDetails.getUsername()));
            order.setCreatedBy(user);
        }

        return orderRepository.save(order);
    }

    public Order updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + userDetails.getUsername()));
            order.updateStatus(newStatus, user);
        } else {
            order.updateStatus(newStatus, null);
        }

        return orderRepository.save(order);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
            .map(this::toOrderResponse)
            .toList();
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
        return toOrderResponse(order);
    }

    public OrderResponse toOrderResponse(Order order) {
        UserRequest createdBy = order.getCreatedBy() != null
            ? new UserRequest(
                order.getCreatedBy().getName(),
                order.getCreatedBy().getEmail(),
                order.getCreatedBy().getPassword(),
                order.getCreatedBy().getRole(),
                order.getCreatedBy().getSections() != null ? order.getCreatedBy().getSections().stream().map(Section::getId).toList() : List.of()
            )
            : null;
        List<ItemResponse> items = order.getItems() != null
            ? order.getItems().stream()
                .map(item -> new ItemResponse(
                    item.getId(), // itemId
                    item.getName(),
                    item.getCurrentStock(),
                    item.getMeasure(),
                    item.getExpireDate(),
                    item.getSupplier() != null ? item.getSupplier().getId() : null,
                    item.getSupplier() != null ? item.getSupplier().getName() : null,
                    null, // sectionId
                    null, // sectionName
                    item.getItemType() != null ? item.getItemType().getId() : null,
                    item.getItemType() != null ? item.getItemType().getName() : null,
                    item.getMinimumStock(),
                    item.getQrCode(),
                    item.getLastUser() != null ? item.getLastUser().getName() : null,
                    item.getLastUpdate()
                ))
                .toList()
            : List.of();
        List<SupplierCompanyResponse> suppliers = order.getSuppliers() != null
            ? order.getSuppliers().stream()
                .map(supplier -> new SupplierCompanyResponse(
                    supplier.getId(),
                    supplier.getName(),
                    supplier.getEmail(),
                    supplier.getPhoneNumber(),
                    supplier.getCnpj(),
                    supplier.getIsActive(),
                    supplier.getRating(),
                    supplier.getLastUpdate(),
                    supplier.getLastUser() != null ? supplier.getLastUser().getName() : null,
                    supplier.getItems() != null ? supplier.getItems().stream().map(Item::getId).toList() : List.of(),
                    supplier.getOrders() != null ? supplier.getOrders().stream().map(Order::getId).toList() : List.of()
                ))
                .toList()
            : List.of();
        return new OrderResponse(
            order.getId(),
            order.getStatus(),
            order.getWithdrawDay(),
            order.getCreatedAt(),
            createdBy,
            items,
            suppliers
        );
    }
}
