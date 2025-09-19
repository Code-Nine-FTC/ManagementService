package com.codenine.managementservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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

    private Integer maximumStock;

    private Integer currentStock = 0;

    private Integer minimumStock;

    private String qrCode;

    private Boolean isActive = true;

    private LocalDateTime lastUpdate = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String archiveInfo;

    @ManyToOne
    private User lastUser;

    @ManyToOne
    private SupplierCompany supplier;

    @ManyToOne
    @JoinTable(name = "item_item_type", joinColumns = @JoinColumn(name = "item_id"), inverseJoinColumns = @JoinColumn(name = "item_type_id"))
    private ItemType itemType;

    @ManyToMany(mappedBy = "items")
    private List<Order> orders;

}
