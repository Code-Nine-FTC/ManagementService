package com.codenine.managementservice.dto.chat;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDTO {
  private Long id;
  private String name;
  private List<ChatUserDTO> participants;
  private LocalDateTime createdAt;
  private LocalDateTime lastMessageAt;
  private ChatMessageDTO lastMessage;
  private Boolean isActive;
}
