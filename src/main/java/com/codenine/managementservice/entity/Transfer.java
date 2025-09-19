package com.codenine.managementservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transfers")
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status;

    private LocalDateTime deliveryDate;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime lastUpdate = LocalDateTime.now();

    @ManyToOne
    private User lastUser;
}
