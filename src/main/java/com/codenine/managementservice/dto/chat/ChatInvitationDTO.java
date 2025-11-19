package com.codenine.managementservice.dto.chat;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatInvitationDTO {
  private Long id;
  private String token;
  private String guestEmail;
  private Long chatRoomId;
  private String chatRoomName;
  private String invitedByName;
  private LocalDateTime createdAt;
  private LocalDateTime expiresAt;
  private Boolean isUsed;
}
