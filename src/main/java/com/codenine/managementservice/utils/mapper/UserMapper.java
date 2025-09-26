package com.codenine.managementservice.utils.mapper;

import java.time.LocalDateTime;
import java.util.List;

import com.codenine.managementservice.dto.user.UserRequest;
import com.codenine.managementservice.dto.user.UserResponse;
import com.codenine.managementservice.entity.Section;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.dto.section.SectionDto;

public class UserMapper {

  public static User toEntity(UserRequest userRequest, String passwordEncoder) {
    User user = new User();
    user.setName(userRequest.name());
    user.setEmail(userRequest.email());
    user.setPassword(passwordEncoder);
    user.setLastUpdate(LocalDateTime.now());
    return user;
  }

  public static User toUpdate(User user, UserRequest userRequest) {
    if (userRequest.name() != null) user.setName(userRequest.name());
    if (userRequest.email() != null) user.setEmail(userRequest.email());
    if (userRequest.password() != null) user.setPassword(userRequest.password());
    if (userRequest.name() != null || userRequest.email() != null || userRequest.password() != null)
      user.setLastUpdate(LocalDateTime.now());
    return user;
  }

  public static UserResponse toResponse(User user) {
    return new UserResponse(
        user.getId(),
        user.getName(),
        user.getEmail(),
        user.getRole(),
        user.getIsActive(),
        user.getSections().stream().map(Section::getId).toList(),
        user.getSections().stream()
            .map(
                section ->
                    new SectionDto(
                            section.getId(), section.getTitle()))
            .toList());
  }

  public static List<UserResponse> toResponse(List<User> users) {
    return users.stream().map(UserMapper::toResponse).toList();
  }
}
