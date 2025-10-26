package com.codenine.managementservice.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
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

  // Order entity no longer references SupplierCompany; remove obsolete mapping to avoid boot
  // errors.
  // If you need to navigate orders by supplier in the future, reintroduce with a proper owning
  // side.
}
