package com.codenine.managementservice.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

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
  
  private final EntityManager entityManager;

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
    // Build dynamic criteria query to avoid binding null parameters without types
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<PurchaseOrder> cq = cb.createQuery(PurchaseOrder.class);
    Root<PurchaseOrder> root = cq.from(PurchaseOrder.class);
    Join<Object, Object> orderJoin = root.join("order");
    Join<Object, Object> supplierJoin = root.join("supplierCompany");
    Join<Object, Object> lastUserJoin = root.join("lastUser");
    Join<Object, Object> createdByJoin = root.join("createdBy");

    Predicate predicate = cb.conjunction();

    if (filterCriteria.supplierCompanyId() != null) {
      predicate = cb.and(predicate, cb.equal(supplierJoin.get("id"), filterCriteria.supplierCompanyId()));
    }
    if (filterCriteria.orderId() != null) {
      predicate = cb.and(predicate, cb.equal(orderJoin.get("id"), filterCriteria.orderId()));
    }
    if (filterCriteria.status() != null) {
      predicate = cb.and(predicate, cb.equal(root.get("status"), filterCriteria.status()));
    }
    if (filterCriteria.emailStatus() != null) {
      predicate = cb.and(predicate, cb.equal(root.get("emailStatus"), filterCriteria.emailStatus()));
    }
    if (filterCriteria.createdAfter() != null) {
      predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("createdAt"), filterCriteria.createdAfter()));
    }
    if (filterCriteria.createdBefore() != null) {
      predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("createdAt"), filterCriteria.createdBefore()));
    }
    if (filterCriteria.year() != null) {
      predicate = cb.and(predicate, cb.equal(root.get("year"), filterCriteria.year()));
    }

    cq.select(root).where(predicate).orderBy(cb.desc(root.get("createdAt")));

    List<PurchaseOrder> results = entityManager.createQuery(cq).getResultList();

  // Map entities to DTO responses (include sender info)
  return results.stream()
    .map(po ->
      new PurchaseOrderResponse(
        po.getId(),
        po.getIssuingBody(),
        po.getCommitmentNoteNumber(),
        po.getYear(),
        po.getProcessNumber(),
        po.getTotalValue(),
        po.getIssueDate(),
        po.getStatus(),
        po.getEmailStatus(),
        po.getCreatedAt(),
        po.getLastUpdate(),
        po.getOrder() != null ? po.getOrder().getId() : null,
        po.getOrder() != null ? po.getOrder().getStatus() : null,
        po.getSupplierCompany() != null ? po.getSupplierCompany().getId() : null,
        po.getSupplierCompany() != null ? po.getSupplierCompany().getName() : null,
        po.getSupplierCompany() != null ? po.getSupplierCompany().getEmail() : null,
        po.getSender() != null ? po.getSender().getId() : null,
        po.getSender() != null ? po.getSender().getName() : null,
        po.getLastUser() != null ? po.getLastUser().getId() : null,
        po.getLastUser() != null ? po.getLastUser().getName() : null,
        po.getCreatedBy() != null ? po.getCreatedBy().getId() : null,
        po.getCreatedBy() != null ? po.getCreatedBy().getName() : null
      ))
    .toList();
  }

  public PurchaseOrderResponse getPurchaseOrderById(Long id) {
    PurchaseOrder po = validateExistence(id);
  PurchaseOrderResponse purchaseOrderResponse =
    new PurchaseOrderResponse(
      po.getId(),
      po.getIssuingBody(),
      po.getCommitmentNoteNumber(),
      po.getYear(),
      po.getProcessNumber(),
      po.getTotalValue(),
      po.getIssueDate(),
      po.getStatus(),
      po.getEmailStatus(),
      po.getCreatedAt(),
      po.getLastUpdate(),
      po.getOrder() != null ? po.getOrder().getId() : null,
      po.getOrder() != null ? po.getOrder().getStatus() : null,
      po.getSupplierCompany() != null ? po.getSupplierCompany().getId() : null,
      po.getSupplierCompany() != null ? po.getSupplierCompany().getName() : null,
      po.getSupplierCompany() != null ? po.getSupplierCompany().getEmail() : null,
      po.getSender() != null ? po.getSender().getId() : null,
      po.getSender() != null ? po.getSender().getName() : null,
      po.getLastUser() != null ? po.getLastUser().getId() : null,
      po.getLastUser() != null ? po.getLastUser().getName() : null,
      po.getCreatedBy() != null ? po.getCreatedBy().getId() : null,
      po.getCreatedBy() != null ? po.getCreatedBy().getName() : null);
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
    // set sender/lastUser before sending so EmailService can rely on sender info
    purchaseOrder.setLastUser(lastUser);
    purchaseOrder.setSender(lastUser);
    purchaseOrder.setLastUpdate(LocalDateTime.now());
    // attempt to send email (EmailService is resilient if some fields are missing)
    emailService.sendCommitmentNoteEmail(purchaseOrder, supplier, supplier.getEmail());
    purchaseOrder.setEmailStatus(EmailStatus.SENT);
    purchaseOrderRepository.save(purchaseOrder);
  }
}
