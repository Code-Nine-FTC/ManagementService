package com.codenine.managementservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "items")
public class Item {
    @GeneratedValue
    private Long id;

    private String name;

    private String measure;

    private Instant expireDate;

    private
}
