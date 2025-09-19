package com.codenine.managementservice.service;

import com.codenine.managementservice.dto.user.Role;
import com.codenine.managementservice.dto.user.UserRequest;
import com.codenine.managementservice.entity.Section;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.repository.UserRepository;

import java.util.List;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private SectionRepository sectionRepository;

  public void createUser(UserRequest userRequest) {
        List<Section> sections = getSectionsByIds(userRequest.sectionIds());
        User user = new User();
        user.setName(userRequest.name());
        user.setEmail(userRequest.email());
        user.setPassword(userRequest.password());
        user.setRole(userRequest.role());
        user.setSections(sections);
        userRepository.save(user);
  }

    public User getUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new NullPointerException("User not found with id: " + id));
    }

    public List<User> getAllUsers(Authentication authentication) {
      User user = (User) authentication.getPrincipal();
        if (user.getRole().equals(Role.MANAGER)) {
            List<Long> sectionIds = user.getSections().stream()
                .map(Section::getId).toList();
            List<User> users = userRepository.findBySections_IdIn(sectionIds);
            return users;
        }
        return userRepository.findAll();
    }

  private List<Section> getSectionsByIds(List<Long> sectionIds) {
      return sectionRepository.findAllById(sectionIds);
  }
}
