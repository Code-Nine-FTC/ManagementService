package com.codenine.managementservice.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "suppliers_companies")
public class SupplierCompany {
  @GeneratedValue @Id private Long id;

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

  @ManyToOne private User lastUser;

  @OneToMany private List<Item> items;

  @ManyToMany(mappedBy = "suppliers")
  private List<Order> orders;
}
