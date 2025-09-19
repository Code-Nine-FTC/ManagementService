package com.codenine.managementservice.entity;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "notifications")
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String text;

  private Long createdAt = Instant.now().getEpochSecond();
}
