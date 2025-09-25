package com.codenine.managementservice.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "sections")
public class Section {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(name = "role_access")
  private Integer roleAccess;

  private Boolean isActive = true;

  private LocalDateTime createdAt = LocalDateTime.now();

  private LocalDateTime lastUpdate = LocalDateTime.now();

  @ManyToOne private User lastUser;

  @ManyToMany(mappedBy = "sections")
  private List<User> users;

  @OneToMany(mappedBy = "section")
  @JsonIgnoreProperties("section")
  private List<ItemType> itemTypes;
}
