package com.codenine.managementservice.entity;

import com.codenine.managementservice.dto.Role;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String password;

    private Role role;

    private Date createdAt;

    private Long lastUpdate;

    private Boolean isActive;

    @ManyToOne
    private Section section;
}
