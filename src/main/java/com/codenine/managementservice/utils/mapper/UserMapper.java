package com.codenine.managementservice.utils.mapper;

import java.time.LocalDateTime;

import com.codenine.managementservice.dto.user.UserRequest;
import com.codenine.managementservice.entity.User;

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
        if (userRequest.name() != null)
            user.setName(userRequest.name());
        if (userRequest.email() != null)
            user.setEmail(userRequest.email());
        if (userRequest.password() != null)
            user.setPassword(userRequest.password());
        if (userRequest.name() != null || userRequest.email() != null || userRequest.password() != null)
            user.setLastUpdate(LocalDateTime.now());
        return user;
    }
}
