package com.codenine.managementservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "unit_destinations")
public class UnitDestination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    private String name;

    private String street;

    private String number;

    private String city;

    private String state;

    private String zipCode;

    private String phoneNumber;
}
