package com.codenine.managementservice.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

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

  @ManyToOne private User lastUser;

  @ManyToOne
  @JoinTable(
      name = "itemtype_section",
      joinColumns = @JoinColumn(name = "item_type_id"),
      inverseJoinColumns = @JoinColumn(name = "section_id"))
  private Section section;

  @OneToMany(mappedBy = "itemType")
  private List<Item> items;
}
