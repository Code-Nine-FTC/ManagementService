package com.codenine.managementservice.entity;

import java.time.LocalDateTime;

import com.codenine.managementservice.dto.purchaseOrder.EmailStatus;
import com.codenine.managementservice.dto.purchaseOrder.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "purchase_orders")
public class PurchaseOrder {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String purchaseOrderNumber;

  private String issuingBody;
  private String commitmentNoteNumber;
  private Integer year;
  private String processNumber;
  private Float totalValue;
  private LocalDateTime issueDate;

  @Enumerated(EnumType.STRING)
  private Status status = Status.PENDING_DELIVERY;

  @Enumerated(EnumType.STRING)
  private EmailStatus emailStatus = EmailStatus.NOT_SENT;

  private LocalDateTime createdAt = LocalDateTime.now();
  private LocalDateTime lastUpdate = LocalDateTime.now();

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = true)
  private User sender;

  @ManyToOne
  @JoinColumn(name = "supplier_company_id")
  private SupplierCompany supplierCompany;

  @ManyToOne private User createdBy;

  @ManyToOne private User lastUser;
}
