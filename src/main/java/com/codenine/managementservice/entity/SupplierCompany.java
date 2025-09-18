package com.codenine.managementservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "suppliers_companies")
public class SupplierCompany {
    @GeneratedValue
    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String cnpj;

    private Boolean isActive = true;

    private Integer rating = 0;

    private LocalDateTime lastUpdate = LocalDateTime.now();

    @ManyToOne
    private User lastUser;

    @OneToMany
    private List<Item> items;

    @ManyToMany(mappedBy = "suppliers")
    private List<Order> orders;
}
