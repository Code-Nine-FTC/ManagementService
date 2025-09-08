package com.codenine.managementservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "sections")
public class Section {
    @Id
    private Long id;

    private String title;

    @OneToMany
    private List<User> users;
}
