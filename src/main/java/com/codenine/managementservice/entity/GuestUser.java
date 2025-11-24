package com.codenine.managementservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "guest_users")
public class GuestUser {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String cpf;

  @Column(nullable = false)
  private Integer age;

  @Column(nullable = false, length = 1)
  private String gender; // M, F, O

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password; // Senha criptografada

  @Column(nullable = false)
  @lombok.Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(nullable = false)
  @lombok.Builder.Default
  private Boolean isActive = true;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "chat_room_id")
  private ChatRoom chatRoom; // Sala de chat associada ao guest
}
