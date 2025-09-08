package com.codenine.managementservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @GeneratedValue
    @Id
    private Long id;

    private Date createdAt;

    private Date lastDay;

    private Date withdrawDay;

    private String status;

    @ManyToOne
    private Item item;

    @ManyToOne
    private Supplier supplier;
}
