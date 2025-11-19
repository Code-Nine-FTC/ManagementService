package com.codenine.managementservice.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatUserDTO {
  private Long id;
  private String name;
  private String email;
}
