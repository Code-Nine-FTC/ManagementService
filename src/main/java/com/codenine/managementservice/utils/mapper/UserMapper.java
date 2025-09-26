package com.codenine.managementservice.utils.mapper;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.codenine.managementservice.dto.user.UserRequest;
import com.codenine.managementservice.entity.User;

public class UserMapper {

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User toEntity(UserRequest userRequest) {
        User user = new User();
        user.setName(userRequest.name());
        user.setEmail(userRequest.email());
        user.setPassword(passwordEncoder.encode(userRequest.password()));
        user.setLastUpdate(LocalDateTime.now());
        return user;
    }

    public UserRequest toUpdate(User user, UserRequest userRequest) {
        if (userRequest.name() != null)
            user.setName(userRequest.name());
        if (userRequest.email() != null)
            user.setEmail(userRequest.email());
        if (userRequest.password() != null)
            user.setPassword(userRequest.password());
        if (userRequest.name() != null || userRequest.email() != null || userRequest.password() != null)
            user.setLastUpdate(LocalDateTime.now());
        return userRequest;
    }
}
