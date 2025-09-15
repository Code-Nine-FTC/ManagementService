package com.codenine.managementservice.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Entity
@Table(name = "items_type")
public class ItemType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Boolean isActive = true;

    private LocalDateTime lastUpdate = LocalDateTime.now();

    @ManyToOne
    private User lastUser;

    @ManyToOne
    private Section section;

    @OneToMany
    private List<Item> items;
}
