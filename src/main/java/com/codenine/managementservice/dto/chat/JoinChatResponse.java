package com.codenine.managementservice.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinChatResponse {
  private String token;
  private ChatRoomDTO chatRoom;
  private String guestEmail;
  private String message;
}
