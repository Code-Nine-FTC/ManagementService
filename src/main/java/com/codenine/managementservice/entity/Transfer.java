package com.codenine.managementservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name =  "transfers")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date createdAt;

    private String status;

    private Date deliveryDate;

    @ManyToOne
    private Item item;

    @ManyToOne
    private UnitDestination unitDestination;

    @ManyToOne
    private User user;
}
