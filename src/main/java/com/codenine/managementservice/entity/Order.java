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

    private long withdrawDay;

    private String status;

    @Column(nullable = false)
    private long createdAt = Instant.now().getEpochSecond();

    @Column(nullable = false)
    private long lastDay = Instant.now().getEpochSecond();

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
