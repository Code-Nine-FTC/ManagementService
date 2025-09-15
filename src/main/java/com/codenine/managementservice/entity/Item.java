package com.codenine.managementservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String measure;

    private LocalDateTime expireDate;

    @Column(nullable = false)
    private Integer currentStock = 0;

    @Column(nullable = false)
    private Integer minimumStock = 0;

    private String qrCode;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private LocalDateTime lastUpdate = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean isArchived = false;

    @ManyToOne
    private User lastUser;

    @ManyToOne
    private SupplierCompany supplier;

    @ManyToOne
    private Section section;

    @ManyToOne
    private ItemType itemType;
    
}
