package com.codenine.managementservice.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteGuestRequest {
  private String guestEmail;
  private String guestName;
}
