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

    private Integer currentStock = 0;

    private Integer minimumStock = 0;

    private String qrCode;

    private Boolean isActive = true;

    private LocalDateTime lastUpdate = LocalDateTime.now();

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
