package com.codenine.managementservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @GeneratedValue
    @Id
    private Long id;

    private LocalDateTime withdrawDay;

    private String status;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime lastDay = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime lastUpdate = LocalDateTime.now();

    @ManyToOne
    private User createdBy;

    @ManyToOne
    private User lastUser;

    @ManyToOne
    private Item item;

    @ManyToOne
    private SupplierCompany supplier;
}
