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
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

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

  @ManyToOne private User lastUser;

  @ManyToOne private SupplierCompany supplier;

  @ManyToOne
  @JoinTable(
      name = "item_item_type",
      joinColumns = @JoinColumn(name = "item_id"),
      inverseJoinColumns = @JoinColumn(name = "item_type_id"))
  private ItemType itemType;

  @ManyToMany(mappedBy = "items")
  private List<Order> orders;
}
