package com.codenine.managementservice.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.codenine.managementservice.dto.purchaseOrder.EmailStatus;
import com.codenine.managementservice.dto.purchaseOrder.PurchaseOrderFilterCriteria;
import com.codenine.managementservice.dto.purchaseOrder.PurchaseOrderRequest;
import com.codenine.managementservice.dto.purchaseOrder.PurchaseOrderResponse;
import com.codenine.managementservice.dto.purchaseOrder.Status;
import com.codenine.managementservice.entity.Order;
import com.codenine.managementservice.entity.PurchaseOrder;
import com.codenine.managementservice.entity.SupplierCompany;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.OrderRepository;
import com.codenine.managementservice.repository.PurchaseOrderRepository;
import com.codenine.managementservice.repository.SupplierCompanyRepository;
import com.codenine.managementservice.utils.mapper.PurchaseOrderMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

  private final PurchaseOrderRepository purchaseOrderRepository;

  private final OrderRepository orderRepository;

  private final SupplierCompanyRepository supplierCompanyRepository;

  private final EmailService emailService;

  public void createPurchaseOrder(PurchaseOrderRequest request, User lastUser) {
    Order order =
        orderRepository
            .findById(request.orderId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException("Order not found with id: " + request.orderId()));
    SupplierCompany supplierCompany =
        supplierCompanyRepository
            .findById(request.supplierCompanyId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Supplier Company not found with id: " + request.supplierCompanyId()));

    PurchaseOrder purchaseOrder =
        PurchaseOrderMapper.toEntity(request, lastUser, order, supplierCompany);
    purchaseOrderRepository.save(purchaseOrder);
  }

  public List<PurchaseOrderResponse> getPurchaseOrders(PurchaseOrderFilterCriteria filterCriteria) {
    List<PurchaseOrderResponse> purchaseOrders =
        purchaseOrderRepository.findAllPurchaseOrders(
            filterCriteria.supplierCompanyId(),
            filterCriteria.orderId(),
            filterCriteria.status(),
            filterCriteria.emailStatus(),
            filterCriteria.createdAfter(),
            filterCriteria.createdBefore(),
            filterCriteria.year());
    return purchaseOrders;
  }

  public PurchaseOrderResponse getPurchaseOrderById(Long id) {
    validateExistence(id);
    PurchaseOrderResponse purchaseOrderResponse =
        purchaseOrderRepository
            .findAllPurchaseOrders(null, id, null, null, null, null, null)
            .stream()
            .findFirst()
            .orElse(null);
    return purchaseOrderResponse;
  }

  public void updateOrderStatus(Long id, Status status, User lastUser) {
    PurchaseOrder purchaseOrder = validateExistence(id);
    purchaseOrder.setStatus(status);
    purchaseOrder.setLastUser(lastUser);
    purchaseOrder.setLastUpdate(LocalDateTime.now());
    purchaseOrderRepository.save(purchaseOrder);
  }

  public void updatePurchaseOrder(Long id, PurchaseOrderRequest request, User lastUser) {
    PurchaseOrder purchaseOrder = validateExistence(id);
    Order order = purchaseOrder.getOrder();
    SupplierCompany supplierCompany = purchaseOrder.getSupplierCompany();
    if (request.orderId() != null) {
      order =
          orderRepository
              .findById(request.orderId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Order not found with id: " + request.orderId()));
    }
    if (request.supplierCompanyId() != null) {
      supplierCompany =
          supplierCompanyRepository
              .findById(request.supplierCompanyId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Supplier Company not found with id: " + request.supplierCompanyId()));
    }
    PurchaseOrderMapper.updateEntity(purchaseOrder, request, lastUser, order, supplierCompany);
    purchaseOrderRepository.save(purchaseOrder);
  }

  private PurchaseOrder validateExistence(Long id) {
    return purchaseOrderRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Purchase Order not found with id: " + id));
  }

  public void sendEmail(Long purchaseOrderId, User lastUser) {
    PurchaseOrder purchaseOrder = validateExistence(purchaseOrderId);
    SupplierCompany supplier = purchaseOrder.getSupplierCompany();
    emailService.sendCommitmentNoteEmail(purchaseOrder, supplier, supplier.getEmail());
    purchaseOrder.setEmailStatus(EmailStatus.SENT);
    purchaseOrder.setLastUser(lastUser);
    purchaseOrder.setSender(lastUser);
    purchaseOrder.setLastUpdate(LocalDateTime.now());
    purchaseOrderRepository.save(purchaseOrder);
  }
}
