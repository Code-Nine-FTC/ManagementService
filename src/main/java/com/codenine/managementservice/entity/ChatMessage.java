package com.codenine.managementservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "chat_messages")
public class ChatMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "chat_room_id", nullable = false)
  private ChatRoom chatRoom;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "sender_id")
  private User sender;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "guest_sender_id")
  private GuestUser guestSender;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(nullable = false)
  private LocalDateTime sentAt = LocalDateTime.now();

  @Column(nullable = false)
  private Boolean isRead = false;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MessageType type = MessageType.TEXT;

  public enum MessageType {
    TEXT,
    SYSTEM
  }
}
