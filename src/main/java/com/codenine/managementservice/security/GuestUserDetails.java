package com.codenine.managementservice.security;

import com.codenine.managementservice.entity.GuestUser;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class GuestUserDetails implements UserDetails {

  private final GuestUser guestUser;

  public GuestUserDetails(GuestUser guestUser) {
    this.guestUser = guestUser;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_GUEST"));
  }

  @Override
  public String getPassword() {
    return guestUser.getPassword();
  }

  @Override
  public String getUsername() {
    return guestUser.getEmail();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return guestUser.getIsActive();
  }

  public GuestUser getGuestUser() {
    return guestUser;
  }

  public Long getGuestId() {
    return guestUser.getId();
  }

  public String getGuestName() {
    return guestUser.getName();
  }
}
