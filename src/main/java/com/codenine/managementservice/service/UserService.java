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
import com.codenine.managementservice.entity.Section;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.SectionRepository;
import com.codenine.managementservice.repository.UserRepository;
import com.codenine.managementservice.utils.mapper.UserMapper;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService {

  @Autowired private UserRepository userRepository;

  @Autowired private SectionRepository sectionRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  public void createUser(UserRequest userRequest) {
    userRepository
        .findByEmail(userRequest.email())
        .ifPresent(
            existingUser -> {
              throw new IllegalArgumentException("Email already in use: " + userRequest.email());
            });

    List<Section> sections = getSectionsByIds(userRequest.sectionIds());
    String encodedPassword = passwordEncoder.encode(userRequest.password());
    User user = UserMapper.toEntity(userRequest, encodedPassword);
    user.setSections(sections);
    userRepository.save(user);
  }

  public UserResponse getUser(Long id) {
    Optional<User> userOptional = userRepository.findById(id);
    if (userOptional.isPresent()) {
      User user = userOptional.get();
        return UserMapper.toResponse(user);
    } else {
      throw new EntityNotFoundException("User not found with id: " + id);
    }
  }

  public List<UserResponse> getAllUsers(Authentication authentication, Long sectionId, boolean isActive) {
    User user = (User) authentication.getPrincipal();
    if (user.getRole().equals(Role.MANAGER)) {
      List<Long> sectionIds = user.getSections().stream().map(Section::getId).toList();
      List<User> users = userRepository.findBySections_IdInAndIsActive(sectionIds, isActive)
              .stream()
              .filter(u -> !u.getRole().equals(Role.ADMIN))
              .toList();
        return UserMapper.toResponse(users);
    }
    List<User> users;
    if (sectionId != null) {
        users = userRepository.findBySections_IdInAndIsActive(List.of(sectionId), isActive);
        } else {
        users = userRepository.findByIsActive(isActive);
    }
    return UserMapper.toResponse(users);
  }

  public void switchUserActive(Long id) {
    User user = getUserById(id);
    user.setIsActive(!user.getIsActive());
    user.setLastUpdate(LocalDateTime.now());
    userRepository.save(user);
  }

  public void updateUser(Long id, UserRequest userRequest) {
    User user = getUserById(id);
    if (userRequest.sectionIds() != null) {
      List<Section> sections = getSectionsByIds(userRequest.sectionIds());
      user.setSections(sections);
    }
    user = UserMapper.toUpdate(user, userRequest);
    user.setLastUpdate(LocalDateTime.now());
    userRepository.save(user);
  }

  private User getUserById(Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
  }

  private List<Section> getSectionsByIds(List<Long> sectionIds) {
    List<Section> sections = sectionRepository.findAllById(sectionIds);
    if (sections.size() != sectionIds.size()) {
      throw new EntityNotFoundException("One or more sections not found with provided IDs");
    }
    return sections;
  }
}
