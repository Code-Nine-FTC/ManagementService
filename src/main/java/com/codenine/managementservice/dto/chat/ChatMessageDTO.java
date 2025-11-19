package com.codenine.managementservice.dto.chat;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
  private Long id;
  private Long chatRoomId;
  private Long senderId;
  private Long guestSenderId;
  private String senderName;
  private String senderType;
  private String content;
  private LocalDateTime sentAt;
  private Boolean isRead;
  private String type;
  private Boolean isFromCurrentUser;
}
