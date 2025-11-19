package com.codenine.managementservice.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestLoginResponse {

  private String token;
  private GuestUserDTO user;
  private ChatRoomDTO chatRoom;
}
