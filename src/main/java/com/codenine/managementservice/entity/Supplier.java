package com.codenine.managementservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "suppliers")
public class Supplier {
    @GeneratedValue
    @Id
    private Long id;

    private String name;

    private String url;

    private String email;

    private String phoneNumber;

    private String cnpj;

    @OneToMany
    private List<Item> items;
}
