package com.codenine.managementservice.entity;

import com.codenine.managementservice.dto.Role;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Collection;
import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Date createdAt;

    private Instant lastUpdate;

    private Boolean isActive;

    @ManyToOne
    private Section section;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        if (section != null) {
            authorities.add(new SimpleGrantedAuthority("SECTION_" + section.getId()));
        }
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
