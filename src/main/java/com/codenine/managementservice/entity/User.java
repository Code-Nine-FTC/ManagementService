package com.codenine.managementservice.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.codenine.managementservice.dto.user.Role;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  private Role role = Role.ASSISTANT;

  private LocalDateTime createdAt = LocalDateTime.now();

  private LocalDateTime lastUpdate = LocalDateTime.now();

  private Boolean isActive = true;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_section",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "section_id"))
  @JsonIgnoreProperties({"users", "itemTypes"})
  private List<Section> sections;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    List<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
    if (sections != null) {
      for (Section section : sections) {
        authorities.add(new SimpleGrantedAuthority("SECTION_" + section.getId()));
      }
    }
    return authorities;
  }

  @Override
  public String getUsername() {
    return email;
  }
}
