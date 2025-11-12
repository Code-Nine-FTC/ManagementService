package com.codenine.managementservice.security;

import com.codenine.managementservice.entity.GuestUser;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.GuestUserRepository;
import com.codenine.managementservice.repository.UserRepository;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;
  private final GuestUserRepository guestUserRepository;

  public CustomUserDetailsService(
      UserRepository userRepository, GuestUserRepository guestUserRepository) {
    this.userRepository = userRepository;
    this.guestUserRepository = guestUserRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    // Primeiro tenta buscar como usuário normal
    Optional<User> user = userRepository.findByEmail(email);
    if (user.isPresent()) {
      return user.get();
    }

    // Se não encontrar, tenta buscar como guest
    Optional<GuestUser> guestUser = guestUserRepository.findByEmail(email);
    if (guestUser.isPresent()) {
      return new GuestUserDetails(guestUser.get());
    }

    throw new UsernameNotFoundException("Usuário não encontrado: " + email);
  }

  public UserDetails loadGuestUserByEmail(String email) throws UsernameNotFoundException {
    GuestUser guestUser =
        guestUserRepository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Guest não encontrado: " + email));
    return new GuestUserDetails(guestUser);
  }
}
