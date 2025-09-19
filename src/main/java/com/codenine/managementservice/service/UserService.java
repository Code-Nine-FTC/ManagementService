package com.codenine.managementservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {

  @Autowired private UserRepository userRepository;

  @Transactional
  public void printUserSections(String email) {
    userRepository
        .findByEmail(email)
        .ifPresent(
            user -> {
              System.out.println("User: " + user.getUsername());
              user.getSections()
                  .forEach(section -> System.out.println("Section: " + section.getTitle()));
            });
  }
}
