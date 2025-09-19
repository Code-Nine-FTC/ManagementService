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
    @JoinTable(name = "itemtype_section", joinColumns = @JoinColumn(name = "item_type_id"), inverseJoinColumns = @JoinColumn(name = "section_id"))
    private Section section;

    @OneToMany(mappedBy = "itemType")
    private List<Item> items;
}
