package com.codenine.managementservice.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Entity
@Data
public class PurchaseOrders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String issuingBody;
    private String commitmentNoteNumber;
    private Integer year;
    private Integer processNumber;
    private Float totalValue;
    private LocalDateTime issueDate;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastUpdate = LocalDateTime.now();

    @OneToOne
    private Order order;

    @ManyToOne
    @JoinColi
    private SupplierCompany supplierCompany;

    @ManyToOne
    private User createdBy;

    @ManyToOne
    private User lastUser;
}
