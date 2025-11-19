package com.codenine.managementservice.dto.chat;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestUserDTO {

  private Long id;
  private String name;
  private String cpf;
  private Integer age;
  private String gender;
  private String email;
  private LocalDateTime createdAt;
  private Boolean isActive;
  private Long chatRoomId;
}
