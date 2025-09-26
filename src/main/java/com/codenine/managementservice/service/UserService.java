package com.codenine.managementservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.dto.user.Role;
import com.codenine.managementservice.dto.user.UserRequest;
import com.codenine.managementservice.dto.user.UserResponse;
import com.codenine.managementservice.dto.user.UserUpdate;
import com.codenine.managementservice.entity.Section;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.SectionRepository;
import com.codenine.managementservice.repository.UserRepository;
import com.codenine.managementservice.utils.mapper.UserMapper;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private SectionRepository sectionRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private UserMapper userMapper;

  public void createUser(UserRequest userRequest) {
    userRepository.findByEmail(userRequest.email())
        .ifPresent(existingUser -> {
          throw new IllegalArgumentException("Email already in use: " + userRequest.email());
        });

    List<Section> sections = getSectionsByIds(userRequest.sectionIds());
    User user = userMapper.toEntity(userRequest);
    user.setSections(sections);
    userRepository.save(user);
  }

  public UserResponse getUser(Long id) {
    Optional<User> userOptional = userRepository.findById(id);
    if (userOptional.isPresent()) {
      User user = userOptional.get();
      UserResponse userResponse = new UserResponse(
          user.getId(),
          user.getName(),
          user.getEmail(),
          user.getRole(),
          user.getIsActive(),
          user.getSections().stream().map(Section::getId).toList(),
          user.getSections().stream()
              .map(
                  section -> new com.codenine.managementservice.dto.section.SectionDto(
                      section.getId(), section.getTitle()))
              .toList());
      return userResponse;
    } else {
      throw new EntityNotFoundException("User not found with id: " + id);
    }
  }

  public List<UserResponse> getAllUsers(Authentication authentication) {
    User user = (User) authentication.getPrincipal();
    if (user.getRole().equals(Role.MANAGER)) {
      List<Long> sectionIds = user.getSections().stream().map(Section::getId).toList();
      List<User> users = userRepository.findBySections_IdIn(sectionIds);
      List<UserResponse> userResponse = users.stream()
          .map(
              u -> new UserResponse(
                  u.getId(),
                  u.getName(),
                  u.getEmail(),
                  u.getRole(),
                  u.getIsActive(),
                  u.getSections().stream().map(Section::getId).toList(),
                  u.getSections().stream()
                      .map(
                          section -> new com.codenine.managementservice.dto.section.SectionDto(
                              section.getId(), section.getTitle()))
                      .toList()))
          .toList();
      return userResponse;
    }
    return userRepository.findAll().stream()
        .map(
            u -> new UserResponse(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getRole(),
                u.getIsActive(),
                u.getSections().stream().map(Section::getId).toList(),
                u.getSections().stream()
                    .map(
                        section -> new com.codenine.managementservice.dto.section.SectionDto(
                            section.getId(), section.getTitle()))
                    .toList()))
        .toList();
  }

  public void disableUser(Long id) {
    User user = getUserById(id);
    user.setIsActive(false);
    user.setLastUpdate(LocalDateTime.now());
    userRepository.save(user);
  }

  public void enableUser(Long id) {
    User user = getUserById(id);
    user.setIsActive(true);
    user.setLastUpdate(LocalDateTime.now());
    userRepository.save(user);
  }

  public List<Section> getSectionsByIds(List<Long> sectionIds) {
    return sectionRepository.findAllById(sectionIds);
  }

  public void updateUser(Long id, UserRequest userRequest) {
    User user = getUserById(id);
    user.setName(userRequest.name());
    user.setPassword(passwordEncoder.encode(userRequest.password()));
    user.setLastUpdate(LocalDateTime.now());
    userRepository.save(user);
  }

  public void updateSections(Long id, List<Long> sectionIds) {
    User user = getUserById(id);
    List<Section> sections = getSectionsByIds(sectionIds);
    user.setSections(sections);
    user.setLastUpdate(LocalDateTime.now());
    userRepository.save(user);
  }

  private User getUserById(Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
  }
}