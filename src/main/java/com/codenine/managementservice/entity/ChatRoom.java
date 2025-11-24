package com.codenine.managementservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Entity
@Table(name = "chat_rooms")
public class ChatRoom {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "chat_room_users",
      joinColumns = @JoinColumn(name = "chat_room_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  private List<User> participants = new ArrayList<>();

  @Column(nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  private LocalDateTime lastMessageAt;

  @Column(nullable = false)
  private Boolean isActive = true;
}
